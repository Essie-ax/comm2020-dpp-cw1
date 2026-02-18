package uk.ac.comm2020;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import uk.ac.comm2020.api.PassportsHandler;
import uk.ac.comm2020.api.ProductsHandler;
import uk.ac.comm2020.api.TemplateByIdHandler;
import uk.ac.comm2020.api.TemplatesHandler;
import uk.ac.comm2020.config.EnvConfig;
import uk.ac.comm2020.controller.AuthController;
import uk.ac.comm2020.controller.ChallengeController;
import uk.ac.comm2020.controller.LeaderboardController;
import uk.ac.comm2020.controller.PassportValidationController;
import uk.ac.comm2020.dao.*;
import uk.ac.comm2020.db.Database;
import uk.ac.comm2020.service.*;
import uk.ac.comm2020.util.Db;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

public class WebApp {

    public static void main(String[] args) throws Exception {
        EnvConfig config = EnvConfig.load();

        int port = 8080;
        String platformPort = System.getenv("PORT");
        if (platformPort != null && !platformPort.isBlank()) {
            try {
                port = Integer.parseInt(platformPort.trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid PORT env var: '" + platformPort + "'. Falling back to APP_PORT/8080.");
                port = config.getInt("APP_PORT", 8080);
            }
        } else {
            port = config.getInt("APP_PORT", 8080);
        }

        // Database connection with fallback
        Database database = null;
        boolean useDb = Db.hasUrl() && Db.tryConnection();
        if (useDb) {
            database = new Database(config);
            System.out.println("Using MySQL (DB connected)");
        } else {
            if (Db.hasUrl()) {
                System.out.println("DB_URL set but connection failed -> Using In-memory DAOs. Start MySQL or remove .env to avoid this message.");
            } else {
                System.out.println("Using In-memory DAOs (no DB_URL)");
            }
        }

        // Initialize DAOs
        UserDao userDao = useDb ? new MySqlUserDao() : new InMemoryUserDao();
        ChallengeDao challengeDao = useDb ? new MySqlChallengeDao() : new InMemoryChallengeDao();
        SubmissionDao submissionDao = useDb ? new MySqlSubmissionDao() : new InMemorySubmissionDao();

        // DAOs from main branch (require database)
        TemplateDao templateDao = useDb ? new TemplateDao(database) : null;
        ProductDao productDao = useDb ? new ProductDao(database) : null;
        PassportDao passportDao = useDb ? new PassportDao(database) : null;
        EvidenceDao evidenceDao = useDb ? new EvidenceDao(database) : null;

        // Initialize Services
        SessionService sessionService = new SessionService(userDao);

        // Challenge and Submission services (Module D - 中4)
        ChallengeService challengeService = new ChallengeService(challengeDao, sessionService);
        // Use real PassportDao/EvidenceDao when DB is available, otherwise mock fallback
        SubmissionService submissionService = useDb
                ? new SubmissionService(submissionDao, challengeDao, sessionService, passportDao, evidenceDao)
                : new SubmissionService(submissionDao, challengeDao, sessionService);
        LeaderboardService leaderboardService = new LeaderboardService(submissionDao);

        // Template service (中1)
        TemplateService templateService = useDb ? new TemplateService(templateDao) : null;

        // Product and Passport services (中1, 中2)
        ProductService productService = useDb ? new ProductService(productDao) : null;
        PassportService passportService = useDb ? new PassportService(passportDao, templateDao, productDao) : null;

        // Validation and Evidence services (中3)
        EvidenceService evidenceService = useDb ? new EvidenceService(evidenceDao) : null;
        ValidationService validationService = useDb ? new ValidationService() : null;
        ScoringService scoringService = useDb ? new ScoringService() : null;
        PassportValidationService passportValidationService = useDb ?
                new PassportValidationService(passportDao, evidenceService, validationService, scoringService) : null;

        // Initialize Controllers
        AuthController authController = new AuthController(sessionService);
        ChallengeController challengeController = new ChallengeController(challengeService, submissionService);
        LeaderboardController leaderboardController = new LeaderboardController(leaderboardService);
        PassportValidationController passportValidationController = useDb ?
                new PassportValidationController(passportValidationService) : null;

        // Create server
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // Register contexts
        server.createContext("/", WebApp::handleStatic);
        server.createContext("/api/auth/login", authController::handleLogin);

        // Challenge and Leaderboard routes (Module D - 中4)
        server.createContext("/api/challenges", challengeController::handleChallenges);
        server.createContext("/api/leaderboard", leaderboardController::handleLeaderboard);

        // Template, Product and Passport routes (中1, 中2, 中3) - only when DB is available
        if (useDb) {
            server.createContext("/api/templates", new TemplatesHandler(templateService));
            server.createContext("/api/templates/", new TemplateByIdHandler(templateService));
            server.createContext("/api/products", new ProductsHandler(productService));
            server.createContext("/api/passports", new PassportsHandler(passportService));
            server.createContext("/api/passports/validate", passportValidationController::handleValidatePassport);
        }

        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();
        System.out.println("Server running: http://localhost:" + port + "/login.html");
    }

    private static void handleStatic(HttpExchange ex) throws IOException {
        if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
            sendText(ex, 405, "Method Not Allowed");
            return;
        }

        String path = ex.getRequestURI().getPath();
        if (path.equals("/") || path.isEmpty()) path = "/login.html";

        if (path.contains("..")) {
            sendText(ex, 400, "Bad Request");
            return;
        }

        String resourcePath = "static" + path;
        InputStream in = WebApp.class.getClassLoader().getResourceAsStream(resourcePath);

        if (in == null) {
            sendText(ex, 404, "Not Found: " + path);
            return;
        }

        byte[] bytes = in.readAllBytes();
        Headers h = ex.getResponseHeaders();
        h.set("Content-Type", guessContentType(path));
        ex.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String guessContentType(String path) {
        if (path.endsWith(".html")) return "text/html; charset=utf-8";
        if (path.endsWith(".js")) return "application/javascript; charset=utf-8";
        if (path.endsWith(".css")) return "text/css; charset=utf-8";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".svg")) return "image/svg+xml; charset=utf-8";
        return "application/octet-stream";
    }

    private static void sendText(HttpExchange ex, int status, String text) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }
}
