package uk.ac.comm2020.controller;

import com.sun.net.httpserver.HttpExchange;
import uk.ac.comm2020.service.SessionService;
import uk.ac.comm2020.util.ApiResponse;
import uk.ac.comm2020.util.HttpUtil;

import java.io.IOException;
import java.util.Map;

public class AuthController {

    private final SessionService sessionService;

    public AuthController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    public void handleLogin(HttpExchange ex) throws IOException {
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            HttpUtil.sendJson(ex, 405, ApiResponse.error("METHOD_NOT_ALLOWED", "Only POST is allowed"));
            return;
        }

        String body = HttpUtil.readBody(ex);
        Map<String, String> req = HttpUtil.parseVerySimpleJson(body);
        String username = req.getOrDefault("username", "").trim();
        String password = req.getOrDefault("password", "").trim();

        if (username.isEmpty() || password.isEmpty()) {
            HttpUtil.sendJson(ex, 400, ApiResponse.error("BAD_REQUEST", "username/password required"));
            return;
        }

        ApiResponse response = sessionService.authenticate(username, password);
        HttpUtil.sendJson(ex, response.getStatus(), response);
    }
}