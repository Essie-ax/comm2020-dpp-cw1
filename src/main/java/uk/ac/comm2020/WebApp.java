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
import uk.ac.comm2020.controller.TemplateController;
import uk.ac.comm2020.dao.InMemoryUserDao;
import uk.ac.comm2020.dao.PassportDao;
import uk.ac.comm2020.dao.ProductDao;
import uk.ac.comm2020.dao.TemplateDao;
import uk.ac.comm2020.db.Database;
import uk.ac.comm2020.service.PassportService;
import uk.ac.comm2020.service.ProductService;
import uk.ac.comm2020.service.SessionService;
import uk.ac.comm2020.service.TemplateService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

public class WebApp {

    public static void main(String[] args) throws IOException {
        EnvConfig config = EnvConfig.load();
        int port = config.getInt("APP_PORT", 8080);

        Database database = new Database(config);

        TemplateDao templateDao = new TemplateDao(database);
        ProductDao productDao = new ProductDao(database);

        SessionService sessionService = new SessionService(new InMemoryUserDao());
        TemplateService templateService = new TemplateService(templateDao);
        ProductService productService = new ProductService(productDao);

        PassportService passportService = new PassportService(
                new PassportDao(database),
                templateDao,
                productDao
        );

        AuthController authController = new AuthController(sessionService);

        TemplateService templateServiceWithSession = new TemplateService(templateDao, sessionService);
        TemplateController templateController = new TemplateController(templateServiceWithSession);

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", WebApp::handleStatic);

        server.createContext("/api/auth/login", authController::handleLogin);

        server.createContext("/api/templates", new TemplatesHandler(templateService));
        server.createContext("/api/templates/", new TemplateByIdHandler(templateService));

        server.createContext("/api/gk/templates", templateController::handleTemplates);

        server.createContext("/api/products", new ProductsHandler(productService));
        server.createContext("/api/passports", new PassportsHandler(passportService));

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
        InputStream in = App.class.getClassLoader().getResourceAsStream(resourcePath);

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
