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
import uk.ac.comm2020.controller.ComparisonController;
import uk.ac.comm2020.controller.LeaderboardController;
import uk.ac.comm2020.controller.PassportReadController;
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

        // Default to in-memory to avoid local DB dependency during CW2 development.
        Database database = null;
        boolean useDb = "true".equalsIgnoreCase(Db.getEnv("USE_DB")) && Db.hasUrl() && Db.tryConnection();
        if (useDb) {
            database = new Database(config);
            System.out.println("Using MySQL (DB connected)");
        } else {
            if ("true".equalsIgnoreCase(Db.getEnv("USE_DB")) && Db.hasUrl()) {
                System.out.println("USE_DB=true but DB connection failed -> Using In-memory DAOs.");
            } else {
                System.out.println("Using In-memory DAOs (default mode)");
            }
        }

        UserDao userDao = useDb ? new MySqlUserDao() : new InMemoryUserDao();
        ChallengeDao challengeDao = useDb ? new MySqlChallengeDao() : new InMemoryChallengeDao();
        SubmissionDao submissionDao = useDb ? new MySqlSubmissionDao() : new InMemorySubmissionDao();

        TemplateDao templateDao = useDb ? new TemplateDao(database) : null;
        ProductDao productDao = useDb ? new ProductDao(database) : null;
        PassportRepository passportRepo = useDb ? new PassportDao(database) : new InMemoryPassportDao();
        EvidenceRepository evidenceRepo = useDb ? new EvidenceDao(database) : new InMemoryEvidenceDao();

        SessionService sessionService = new SessionService(userDao);

        ChallengeService challengeService = new ChallengeService(challengeDao, sessionService);
        SubmissionService submissionService = useDb
                ? new SubmissionService(submissionDao, challengeDao, sessionService, (PassportDao) passportRepo, (EvidenceDao) evidenceRepo)
                : new SubmissionService(submissionDao, challengeDao, sessionService);
        LeaderboardService leaderboardService = new LeaderboardService(submissionDao);

        TemplateService templateService = useDb ? new TemplateService(templateDao) : null;
        ProductService productService = useDb ? new ProductService(productDao) : null;
        PassportService passportService = useDb ? new PassportService((PassportDao) passportRepo, templateDao, productDao) : null;

        EvidenceService evidenceService = new EvidenceService(evidenceRepo);
        ValidationService validationService = new ValidationService();
        ScoringService scoringService = new ScoringService();
        PassportValidationService passportValidationService =
                new PassportValidationService(passportRepo, evidenceService, validationService, scoringService);

        AuthController authController = new AuthController(sessionService);
        ChallengeController challengeController = new ChallengeController(challengeService, submissionService);
        LeaderboardController leaderboardController = new LeaderboardController(leaderboardService);
        PassportValidationController passportValidationController =
                new PassportValidationController(passportValidationService);

        ComparisonService comparisonService = new ComparisonService(passportRepo);
        ComparisonController comparisonController = new ComparisonController(comparisonService);
        PassportReadController passportReadController = new PassportReadController(passportRepo);

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", WebApp::handleStatic);
        server.createContext("/api/auth/login", authController::handleLogin);

        server.createContext("/api/challenges", challengeController::handleChallenges);
        server.createContext("/api/leaderboard", leaderboardController::handleLeaderboard);

        server.createContext("/api/passports/validate", passportValidationController::handleValidatePassport);
        server.createContext("/api/passports/compare", comparisonController::handleCompare);

        // Without DB the PassportsHandler isn't registered, so we need our own read endpoint.
        if (!useDb) {
            server.createContext("/api/passports/", passportReadController::handleGetById);
        }

        // These handlers depend on real DB DAOs, so keep them disabled in in-memory mode.
        if (useDb) {
            server.createContext("/api/templates", new TemplatesHandler(templateService));
            server.createContext("/api/templates/", new TemplateByIdHandler(templateService));
            server.createContext("/api/products", new ProductsHandler(productService));
            server.createContext("/api/passports", new PassportsHandler(passportService));
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
