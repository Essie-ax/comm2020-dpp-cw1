package uk.ac.comm2020;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.InetSocketAddress;

import java.nio.charset.StandardCharsets;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import uk.ac.comm2020.controller.AuthController;
import uk.ac.comm2020.controller.TemplateController;
import uk.ac.comm2020.dao.InMemoryTemplateDao;
import uk.ac.comm2020.model.Role;
import uk.ac.comm2020.service.SessionService;
import uk.ac.comm2020.service.TemplateService;


public class WebApp {
    private static final Map<String, Session> sessions = new HashMap<>();

private static class Session {
    final long userId;
    final Role role;
    final String username;

    Session(long userId, Role role, String username) {
        this.userId = userId;
        this.role = role;
        this.username = username;
    }
}


    public static void main(String[] args) throws Exception {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // Initialize services and controllers
        SessionService sessionService = new SessionService();
        InMemoryTemplateDao templateDao = new InMemoryTemplateDao();
        TemplateService templateService = new TemplateService(templateDao, sessionService);

        AuthController authController = new AuthController(sessionService);
        TemplateController templateController = new TemplateController(templateService);

        // Register contexts
        server.createContext("/", WebApp::handleStatic);
        server.createContext("/api/auth/login", authController::handleLogin);
        server.createContext("/api/templates", templateController::handleTemplates);

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

  private static void sendJson(HttpExchange ex, int status, String json) throws IOException {
    byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
    ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
    ex.sendResponseHeaders(status, bytes.length);
    try (OutputStream os = ex.getResponseBody()) {
      os.write(bytes);
    }
  }

  private static String jsonError(String code, String message) {
    return "{"
        + "\"success\":false,"
        + "\"error\":{"
        + "\"code\":\"" + escape(code) + "\","
        + "\"message\":\"" + escape(message) + "\","
        + "\"details\":{}"
        + "}"
        + "}";
  }

  
  private static Map<String, String> parseVerySimpleJson(String body) {
    Map<String, String> m = new HashMap<>();
    if (body == null) return m;
    String s = body.trim();
    if (!s.startsWith("{") || !s.endsWith("}")) return m;
    s = s.substring(1, s.length() - 1).trim();
    if (s.isEmpty()) return m;

    
    String[] parts = s.split(",");
    for (String p : parts) {
      String[] kv = p.split(":", 2);
      if (kv.length != 2) continue;
      String k = stripQuotes(kv[0].trim());
      String v = stripQuotes(kv[1].trim());
      m.put(k, v);
    }
    return m;
  }

  private static String stripQuotes(String s) {
    if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
      return s.substring(1, s.length() - 1);
    }
    return s;
  }

  private static String escape(String s) {
    return s.replace("\\", "\\\\").replace("\"", "\\\"");
  }
private static void handleTemplates(HttpExchange ex) throws IOException {
  if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
    sendJson(ex, 405, jsonError("METHOD_NOT_ALLOWED", "Only GET is allowed"));
    return;
  }

  String auth = ex.getRequestHeaders().getFirst("Authorization");
  String token = extractBearerToken(auth);

  Session s = (token == null) ? null : sessions.get(token);
  if (s == null) {
    sendJson(ex, 401, jsonError("UNAUTHORIZED", "Missing or invalid token"));
    return;
  }

  // 只有 GameKeeper 能访问
  if (s.role != Role.GAME_KEEPER) {
    sendJson(ex, 403, jsonError("FORBIDDEN", "GameKeeper role required"));
    return;
  }

  String category = getQueryParam(ex.getRequestURI().getRawQuery(), "category");
  if (category == null || category.isBlank()) category = "Battery";

  String resp = "{"
      + "\"success\":true,"
      + "\"data\":{"
      + "\"templates\":[{"
      + "\"templateId\":1,"
      + "\"category\":\"" + escape(category) + "\","
      + "\"requiredFields\":[\"name\",\"brand\",\"origin\",\"chemistry\"],"
      + "\"optionalFields\":[\"recyclability\",\"warrantyMonths\"],"
      + "\"ruleSetId\":1"
      + "}]"
      + "}"
      + "}";

  sendJson(ex, 200, resp);
}

private static String extractBearerToken(String authHeader) {
  if (authHeader == null) return null;
  String p = "Bearer ";
  if (!authHeader.startsWith(p)) return null;
  return authHeader.substring(p.length()).trim();
}

private static String getQueryParam(String query, String key) {
  if (query == null || query.isBlank()) return null;
  String[] parts = query.split("&");
  for (String part : parts) {
    String[] kv = part.split("=", 2);
    if (kv.length == 2 && kv[0].equals(key)) {
      return java.net.URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
    }
  }
  return null;
}
}
