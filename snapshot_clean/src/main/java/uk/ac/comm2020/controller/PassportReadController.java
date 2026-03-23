package uk.ac.comm2020.controller;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sun.net.httpserver.HttpExchange;
import uk.ac.comm2020.dao.PassportRepository;
import uk.ac.comm2020.model.Passport;
import uk.ac.comm2020.util.ApiResponse;
import uk.ac.comm2020.util.HttpUtil;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

// Serves GET /api/passports/{id} in in-memory mode.
// The DB mode already has PassportsHandler covering this route.
public class PassportReadController {

    private final PassportRepository passportRepo;

    public PassportReadController(PassportRepository passportRepo) {
        this.passportRepo = passportRepo;
    }

    public void handleGetById(HttpExchange ex) throws IOException {
        if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
            HttpUtil.sendJson(ex, 405, ApiResponse.error("METHOD_NOT_ALLOWED", "Only GET is allowed"));
            return;
        }

        // Extract ID from the end of the path, e.g. /api/passports/3 -> "3"
        String path = ex.getRequestURI().getPath();
        String[] parts = path.split("/");
        String idStr = parts[parts.length - 1];

        long passportId;
        try {
            passportId = Long.parseLong(idStr);
        } catch (NumberFormatException e) {
            HttpUtil.sendJson(ex, 400, ApiResponse.error("BAD_REQUEST", "Invalid passport ID"));
            return;
        }

        try {
            Optional<Passport> opt = passportRepo.findById(passportId);
            if (opt.isEmpty()) {
                HttpUtil.sendJson(ex, 404, ApiResponse.error("NOT_FOUND", "Passport not found: " + passportId));
                return;
            }
            HttpUtil.sendJson(ex, 200, ApiResponse.ok(passportToMap(opt.get())));
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.sendJson(ex, 500, ApiResponse.error("INTERNAL_ERROR", "Failed to load passport"));
        }
    }

    private Map<String, Object> passportToMap(Passport p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("passportId", p.getPassportId());
        m.put("productId", p.getProductId());
        m.put("templateId", p.getTemplateId());
        m.put("status", p.getStatus());
        m.put("completenessScore", p.getCompletenessScore());
        m.put("confidenceScore", p.getConfidenceScore());
        m.put("fields", fieldsToMap(p.getFields()));
        return m;
    }

    private Map<String, Object> fieldsToMap(JsonObject fields) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : fields.entrySet()) {
            JsonElement el = entry.getValue();
            if (el.isJsonPrimitive()) {
                JsonPrimitive prim = el.getAsJsonPrimitive();
                if (prim.isBoolean()) m.put(entry.getKey(), prim.getAsBoolean());
                else if (prim.isNumber()) m.put(entry.getKey(), prim.getAsDouble());
                else m.put(entry.getKey(), prim.getAsString());
            } else {
                m.put(entry.getKey(), el.toString());
            }
        }
        return m;
    }
}
