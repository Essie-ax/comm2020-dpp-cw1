package uk.ac.comm2020.controller;

import uk.ac.comm2020.service.ChallengeService;
import uk.ac.comm2020.service.SubmissionService;
import uk.ac.comm2020.util.HttpUtil;
import uk.ac.comm2020.util.ApiResponse;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.util.Map;

public class ChallengeController {

    private final ChallengeService challengeService;
    private final SubmissionService submissionService;

    public ChallengeController(ChallengeService challengeService, SubmissionService submissionService) {
        this.challengeService = challengeService;
        this.submissionService = submissionService;
    }

    /** Routes /api/challenges, /api/challenges/{id}, /api/challenges/{id}/submit. */
    public void handleChallenges(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        String method = ex.getRequestMethod().toUpperCase();

        // --- POST /api/challenges/{id}/submit ---
        if (path.matches("/api/challenges/\\d+/submit")) {
            if (!"POST".equals(method)) {
                HttpUtil.sendJson(ex, 405, ApiResponse.error("METHOD_NOT_ALLOWED", "Only POST allowed"));
                return;
            }
            String idStr = path.replaceAll("/api/challenges/(\\d+)/submit", "$1");
            long challengeId = Long.parseLong(idStr);

            String auth = ex.getRequestHeaders().getFirst("Authorization");
            String token = HttpUtil.extractBearerToken(auth);
            String body = HttpUtil.readBody(ex);
            Map<String, String> params = HttpUtil.parseVerySimpleJson(body);

            ApiResponse res = submissionService.submit(token, challengeId, params);
            int status = res.isSuccess() ? 201 : res.getStatus();
            HttpUtil.sendJson(ex, status, res);
            return;
        }

        // --- GET /api/challenges/{id} (detail) ---
        if (path.matches("/api/challenges/\\d+")) {
            if (!"GET".equals(method)) {
                HttpUtil.sendJson(ex, 405, ApiResponse.error("METHOD_NOT_ALLOWED", "Only GET allowed"));
                return;
            }
            long id = Long.parseLong(path.substring("/api/challenges/".length()));
            ApiResponse res = challengeService.getChallengeById(id);
            HttpUtil.sendJson(ex, res.getStatus(), res);
            return;
        }

        // --- POST /api/challenges (create) ---
        if ("POST".equals(method)) {
            String auth = ex.getRequestHeaders().getFirst("Authorization");
            String token = HttpUtil.extractBearerToken(auth);
            String body = HttpUtil.readBody(ex);
            Map<String, String> params = HttpUtil.parseVerySimpleJson(body);

            ApiResponse res = challengeService.createChallenge(token, params);
            int status = res.isSuccess() ? 201 : res.getStatus();
            HttpUtil.sendJson(ex, status, res);
            return;
        }

        // --- GET /api/challenges?category=... (list) ---
        if ("GET".equals(method)) {
            String category = getQueryParam(ex, "category");
            ApiResponse res = challengeService.getChallenges(category);
            HttpUtil.sendJson(ex, 200, res);
            return;
        }

        HttpUtil.sendJson(ex, 405, ApiResponse.error("METHOD_NOT_ALLOWED", "Use GET or POST"));
    }

    private String getQueryParam(HttpExchange ex, String key) {
        String query = ex.getRequestURI().getQuery();
        if (query == null) return null;
        for (String param : query.split("&")) {
            String[] kv = param.split("=", 2);
            if (kv.length == 2 && key.equals(kv[0])) {
                return kv[1];
            }
        }
        return null;
    }
}
