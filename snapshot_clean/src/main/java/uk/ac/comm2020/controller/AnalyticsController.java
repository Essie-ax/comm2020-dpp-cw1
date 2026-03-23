package uk.ac.comm2020.controller;

import com.sun.net.httpserver.HttpExchange;
import uk.ac.comm2020.service.AnalyticsService;
import uk.ac.comm2020.service.SessionService;
import uk.ac.comm2020.service.SessionService.Session;
import uk.ac.comm2020.util.ApiResponse;
import uk.ac.comm2020.util.HttpUtil;

import java.io.IOException;
import java.util.Map;

public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final SessionService sessionService;

    public AnalyticsController(AnalyticsService analyticsService, SessionService sessionService) {
        this.analyticsService = analyticsService;
        this.sessionService = sessionService;
    }

    public void handleAnalytics(HttpExchange ex) throws IOException {
        if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
            HttpUtil.sendJson(ex, 405, ApiResponse.error("METHOD_NOT_ALLOWED", "Use GET"));
            return;
        }

        String token = HttpUtil.extractBearerToken(ex.getRequestHeaders().getFirst("Authorization"));
        Session session = sessionService.validateToken(token);
        if (session == null) {
            HttpUtil.sendJson(ex, 401, ApiResponse.error("UNAUTHORIZED", "Login required"));
            return;
        }

        Map<String, Object> data = analyticsService.getAnalytics();
        HttpUtil.sendJson(ex, 200, ApiResponse.ok(data));
    }
}
