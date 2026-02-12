package uk.ac.comm2020;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.InetSocketAddress;

import java.nio.charset.StandardCharsets;


import uk.ac.comm2020.controller.AuthController;
import uk.ac.comm2020.controller.ChallengeController;
import uk.ac.comm2020.controller.LeaderboardController;
import uk.ac.comm2020.controller.TemplateController;
import uk.ac.comm2020.dao.ChallengeDao;
import uk.ac.comm2020.dao.InMemoryChallengeDao;
import uk.ac.comm2020.dao.InMemorySubmissionDao;
import uk.ac.comm2020.dao.InMemoryTemplateDao;
import uk.ac.comm2020.dao.InMemoryUserDao;
import uk.ac.comm2020.dao.MySqlChallengeDao;
import uk.ac.comm2020.dao.MySqlSubmissionDao;
import uk.ac.comm2020.dao.MySqlTemplateDao;
import uk.ac.comm2020.dao.MySqlUserDao;
import uk.ac.comm2020.dao.SubmissionDao;
import uk.ac.comm2020.dao.TemplateDao;
import uk.ac.comm2020.dao.UserDao;
import uk.ac.comm2020.service.ChallengeService;
import uk.ac.comm2020.service.LeaderboardService;
import uk.ac.comm2020.service.SessionService;
import uk.ac.comm2020.service.SubmissionService;
import uk.ac.comm2020.service.TemplateService;
import uk.ac.comm2020.util.Db;


public class WebApp {

    public static void main(String[] args) throws Exception {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        boolean useDb = Db.hasUrl() && Db.tryConnection();
        UserDao userDao = useDb ? new MySqlUserDao() : new InMemoryUserDao();
        TemplateDao templateDao = useDb ? new MySqlTemplateDao() : new InMemoryTemplateDao();
        ChallengeDao challengeDao = useDb ? new MySqlChallengeDao() : new InMemoryChallengeDao();
        SubmissionDao submissionDao = useDb ? new MySqlSubmissionDao() : new InMemorySubmissionDao();
        if (useDb) {
            System.out.println("Using MySQL (UserDao + Template DAO, DB connected)");
        } else {
            if (Db.hasUrl()) {
                System.out.println("DB_URL set but connection failed -> Using In-memory (UserDao + Template DAO). Start MySQL or remove .env to avoid this message.");
            } else {
                System.out.println("Using In-memory (UserDao + Template DAO, no DB_URL)");
            }
        }

        SessionService sessionService = new SessionService(userDao);
        TemplateService templateService = new TemplateService(templateDao, sessionService);
        ChallengeService challengeService = new ChallengeService(challengeDao, sessionService);
        SubmissionService submissionService = new SubmissionService(submissionDao, challengeDao, sessionService);
        LeaderboardService leaderboardService = new LeaderboardService(submissionDao);

        AuthController authController = new AuthController(sessionService);
        TemplateController templateController = new TemplateController(templateService);
        ChallengeController challengeController = new ChallengeController(challengeService, submissionService);
        LeaderboardController leaderboardController = new LeaderboardController(leaderboardService);

        // Register contexts
        server.createContext("/", WebApp::handleStatic);
        server.createContext("/api/auth/login", authController::handleLogin);
        server.createContext("/api/templates", templateController::handleTemplates);
        server.createContext("/api/challenges", challengeController::handleChallenges);
        server.createContext("/api/leaderboard", leaderboardController::handleLeaderboard);

        server.setExecutor(null);
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
