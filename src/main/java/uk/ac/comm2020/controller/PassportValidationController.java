package uk.ac.comm2020.controller;

import com.sun.net.httpserver.HttpExchange;
import uk.ac.comm2020.service.PassportValidationService;
import uk.ac.comm2020.service.ValidationResult;
import uk.ac.comm2020.util.ApiResponse;
import uk.ac.comm2020.util.HttpUtil;

import java.io.IOException;
import java.util.Map;

public class PassportValidationController {

    private final PassportValidationService passportValidationService;

    public PassportValidationController(PassportValidationService passportValidationService) {
        this.passportValidationService = passportValidationService;
    }

    // POST /api/passports/validate  body: {"passportId":"123"}
    public void handleValidatePassport(HttpExchange ex) throws IOException {
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            HttpUtil.sendJson(ex, 405, ApiResponse.error("METHOD_NOT_ALLOWED", "Only POST is allowed"));
            return;
        }

        // optional: require login (same style as TemplateController)
        String auth = ex.getRequestHeaders().getFirst("Authorization");
        String token = HttpUtil.extractBearerToken(auth);
        if (token == null || token.isBlank()) {
            HttpUtil.sendJson(ex, 401, ApiResponse.error("UNAUTHORIZED", "Missing Bearer token"));
            return;
        }

        String body = HttpUtil.readBody(ex);
        Map<String, String> req = HttpUtil.parseVerySimpleJson(body);

        String passportIdStr = req.getOrDefault("passportId", "").trim();
        if (passportIdStr.isEmpty()) {
            HttpUtil.sendJson(ex, 400, ApiResponse.error("BAD_REQUEST", "passportId required"));
            return;
        }

        long passportId;
        try {
            passportId = Long.parseLong(passportIdStr);
        } catch (NumberFormatException e) {
            HttpUtil.sendJson(ex, 400, ApiResponse.error("BAD_REQUEST", "passportId must be a number"));
            return;
        }

        try {
            ValidationResult result = passportValidationService.validatePassport(passportId);

java.util.Map<String, Object> data = new java.util.HashMap<>();
data.put("errors", result.getErrors());
data.put("completenessScore", result.getCompletenessScore());
data.put("confidenceScore", result.getConfidenceScore());

HttpUtil.sendJson(ex, 200, ApiResponse.ok(data));

        } catch (IllegalArgumentException e) {
            HttpUtil.sendJson(ex, 404, ApiResponse.error("NOT_FOUND", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.sendJson(ex, 500, ApiResponse.error("INTERNAL_ERROR", "Validation failed"));
        }
    }
}
