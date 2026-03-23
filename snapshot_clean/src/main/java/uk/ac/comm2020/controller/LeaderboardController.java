package uk.ac.comm2020.controller;

import uk.ac.comm2020.service.LeaderboardService;
import uk.ac.comm2020.util.HttpUtil;
import uk.ac.comm2020.util.ApiResponse;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;

public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    /** Routes GET /api/leaderboard/challenge/{id}. */
    public void handleLeaderboard(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        String method = ex.getRequestMethod().toUpperCase();

        if (!"GET".equals(method)) {
            HttpUtil.sendJson(ex, 405, ApiResponse.error("METHOD_NOT_ALLOWED", "Only GET allowed"));
            return;
        }

        // Match /api/leaderboard/challenge/{id}
        if (path.matches("/api/leaderboard/challenge/\\d+")) {
            String idStr = path.substring("/api/leaderboard/challenge/".length());
            long challengeId = Long.parseLong(idStr);
            ApiResponse res = leaderboardService.getChallengeLeaderboard(challengeId);
            HttpUtil.sendJson(ex, res.getStatus(), res);
            return;
        }

        HttpUtil.sendJson(ex, 400, ApiResponse.error("BAD_REQUEST", "Use /api/leaderboard/challenge/{id}"));
    }
}
