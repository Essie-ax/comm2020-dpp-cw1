package uk.ac.comm2020.service;

import uk.ac.comm2020.dao.SubmissionDao;
import uk.ac.comm2020.util.ApiResponse;

import java.util.*;

public class LeaderboardService {

    private final SubmissionDao submissionDao;

    public LeaderboardService(SubmissionDao submissionDao) {
        this.submissionDao = submissionDao;
    }

    /** Get leaderboard for a specific challenge, sorted by score desc then time asc. */
    public ApiResponse getChallengeLeaderboard(long challengeId) {
        if (challengeId <= 0) {
            return ApiResponse.error("BAD_REQUEST", "challengeId must be positive");
        }

        List<Map<String, Object>> submissions = submissionDao.getSubmissionsByChallenge(challengeId);

        // Sort: score desc, then submittedAt asc (earlier submission ranks higher)
        submissions.sort((a, b) -> {
            int scoreA = (int) a.get("score");
            int scoreB = (int) b.get("score");
            if (scoreA != scoreB) return Integer.compare(scoreB, scoreA);
            String timeA = String.valueOf(a.get("submittedAt"));
            String timeB = String.valueOf(b.get("submittedAt"));
            return timeA.compareTo(timeB);
        });

        // Build ranked list
        Object[] rows = new Object[submissions.size()];
        for (int i = 0; i < submissions.size(); i++) {
            Map<String, Object> s = submissions.get(i);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("rank", i + 1);
            row.put("submissionId", s.get("submissionId"));
            row.put("playerId", s.get("submittedBy"));
            row.put("score", s.get("score"));
            row.put("outcome", s.get("outcome"));
            row.put("submittedAt", s.get("submittedAt"));
            rows[i] = row;
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("challengeId", challengeId);
        data.put("entries", rows);
        String msg = rows.length == 0 ? "No submissions yet for this challenge" : rows.length + " entries";
        data.put("message", msg);
        return ApiResponse.ok(data);
    }
}
