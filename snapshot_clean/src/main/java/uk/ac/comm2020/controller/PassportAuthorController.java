package uk.ac.comm2020.controller;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import uk.ac.comm2020.dao.PassportRepository;
import uk.ac.comm2020.model.Passport;
import uk.ac.comm2020.service.SessionService;
import uk.ac.comm2020.util.ApiResponse;
import uk.ac.comm2020.util.HttpUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class PassportAuthorController {

    private final PassportRepository passportRepo;
    private final SessionService sessionService;

    public PassportAuthorController(PassportRepository passportRepo, SessionService sessionService) {
        this.passportRepo = passportRepo;
        this.sessionService = sessionService;
    }

    public void handleAuthor(HttpExchange ex) throws IOException {
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            HttpUtil.sendJson(ex, 405, ApiResponse.error("METHOD_NOT_ALLOWED", "Only POST is allowed"));
            return;
        }

        String authHeader = ex.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            HttpUtil.sendJson(ex, 401, ApiResponse.error("UNAUTHORIZED", "Missing token"));
            return;
        }

        String token = authHeader.substring(7);
        SessionService.Session session = sessionService.validateToken(token);
        if (session == null) {
            HttpUtil.sendJson(ex, 401, ApiResponse.error("UNAUTHORIZED", "Invalid token"));
            return;
        }

        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        JsonObject reqJson;
        try {
            reqJson = JsonParser.parseString(body).getAsJsonObject();
        } catch (Exception e) {
            HttpUtil.sendJson(ex, 400, ApiResponse.error("BAD_REQUEST", "Invalid JSON"));
            return;
        }

        JsonObject fields = reqJson.has("fields") ? reqJson.getAsJsonObject("fields") : new JsonObject();
        if (!fields.has("name") || !fields.has("brand") || !fields.has("origin")) {
            HttpUtil.sendJson(ex, 400, ApiResponse.error("BAD_REQUEST", "name, brand and origin are required"));
            return;
        }

        try {
            long userId = session.userId;
            Passport draft = passportRepo.createDraft(0, 0, userId);

            double completeness = calcCompleteness(fields);
            passportRepo.updateFields(draft.getPassportId(), fields.toString(), completeness, 0.0);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("passportId", draft.getPassportId());
            data.put("completeness", completeness);
            HttpUtil.sendJson(ex, 200, ApiResponse.ok(data));
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.sendJson(ex, 500, ApiResponse.error("SERVER_ERROR", "Failed to create passport"));
        }
    }

    private double calcCompleteness(JsonObject fields) {
        String[] allKeys = {"name", "brand", "category", "origin", "weight",
                "chemistry", "recyclable", "recyclable_percentage",
                "end_of_life", "manufacture_date", "expiry_date", "organic"};
        int filled = 0;
        for (String key : allKeys) {
            if (fields.has(key)) {
                JsonElement el = fields.get(key);
                if (!el.isJsonNull()) filled++;
            }
        }
        return Math.round((double) filled / allKeys.length * 100.0);
    }
}
