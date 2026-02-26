package uk.ac.comm2020.controller;

import com.sun.net.httpserver.HttpExchange;
import uk.ac.comm2020.service.ComparisonService;
import uk.ac.comm2020.util.ApiResponse;
import uk.ac.comm2020.util.HttpUtil;
import uk.ac.comm2020.util.RequestUtil;

import java.io.IOException;
import java.util.Map;

public class ComparisonController {

    private final ComparisonService comparisonService;

    public ComparisonController(ComparisonService comparisonService) {
        this.comparisonService = comparisonService;
    }

    public void handleCompare(HttpExchange ex) throws IOException {
        if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
            HttpUtil.sendJson(ex, 405, ApiResponse.error("METHOD_NOT_ALLOWED", "Only GET is allowed"));
            return;
        }

        String auth = ex.getRequestHeaders().getFirst("Authorization");
        String token = HttpUtil.extractBearerToken(auth);
        if (token == null || token.isBlank()) {
            HttpUtil.sendJson(ex, 401, ApiResponse.error("UNAUTHORIZED", "Missing Bearer token"));
            return;
        }

        Map<String, String> params = RequestUtil.parseQuery(ex.getRequestURI());
        String id1Str = params.getOrDefault("id1", "").trim();
        String id2Str = params.getOrDefault("id2", "").trim();

        if (id1Str.isEmpty() || id2Str.isEmpty()) {
            HttpUtil.sendJson(ex, 400, ApiResponse.error("BAD_REQUEST", "id1 and id2 are required"));
            return;
        }

        long id1, id2;
        try {
            id1 = Long.parseLong(id1Str);
            id2 = Long.parseLong(id2Str);
        } catch (NumberFormatException e) {
            HttpUtil.sendJson(ex, 400, ApiResponse.error("BAD_REQUEST", "id1 and id2 must be numbers"));
            return;
        }

        try {
            Map<String, Object> result = comparisonService.compare(id1, id2);
            HttpUtil.sendJson(ex, 200, ApiResponse.ok(result));
        } catch (IllegalArgumentException e) {
            HttpUtil.sendJson(ex, 404, ApiResponse.error("NOT_FOUND", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.sendJson(ex, 500, ApiResponse.error("INTERNAL_ERROR", "Comparison failed"));
        }
    }
}
