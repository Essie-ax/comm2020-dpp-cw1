package uk.ac.comm2020.service;

import uk.ac.comm2020.dao.ChallengeDao;
import uk.ac.comm2020.dao.SubmissionDao;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsService {

    private final SubmissionDao submissionDao;
    private final ChallengeDao challengeDao;

    public AnalyticsService(SubmissionDao submissionDao, ChallengeDao challengeDao) {
        this.submissionDao = submissionDao;
        this.challengeDao = challengeDao;
    }

    public Map<String, Object> getAnalytics() {
        List<Map<String, Object>> challenges = challengeDao.getChallenges(null);
        List<Map<String, Object>> allSubs = submissionDao.getAllSubmissions();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalChallenges", challenges.size());
        result.put("totalSubmissions", allSubs.size());
        result.put("avgScore", calcAvgScore(allSubs));
        result.put("passRate", calcPassRate(allSubs));
        result.put("scoreDistribution", buildScoreDistribution(allSubs));
        result.put("challenges", buildChallengeBreakdown(challenges));
        return result;
    }

    private double calcAvgScore(List<Map<String, Object>> subs) {
        if (subs.isEmpty()) return 0.0;
        int total = 0;
        for (Map<String, Object> s : subs) {
            total += (int) s.get("score");
        }
        return Math.round((double) total / subs.size() * 10.0) / 10.0;
    }

    private double calcPassRate(List<Map<String, Object>> subs) {
        if (subs.isEmpty()) return 0.0;
        int passed = 0;
        for (Map<String, Object> s : subs) {
            if ("PASS".equals(s.get("outcome"))) passed++;
        }
        return Math.round((double) passed / subs.size() * 1000.0) / 10.0;
    }

    private List<Map<String, Object>> buildScoreDistribution(List<Map<String, Object>> subs) {
        int[] buckets = new int[4]; // 0-24, 25-49, 50-74, 75-100
        for (Map<String, Object> s : subs) {
            int score = (int) s.get("score");
            if (score < 25) buckets[0]++;
            else if (score < 50) buckets[1]++;
            else if (score < 75) buckets[2]++;
            else buckets[3]++;
        }
        String[] labels = {"0-24", "25-49", "50-74", "75-100"};
        List<Map<String, Object>> dist = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Map<String, Object> bucket = new LinkedHashMap<>();
            bucket.put("range", labels[i]);
            bucket.put("count", buckets[i]);
            dist.add(bucket);
        }
        return dist;
    }

    private List<Map<String, Object>> buildChallengeBreakdown(List<Map<String, Object>> challenges) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> c : challenges) {
            long cid = (long) c.get("challengeId");
            List<Map<String, Object>> subs = submissionDao.getSubmissionsByChallenge(cid);

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("challengeId", cid);
            row.put("title", c.get("title"));
            row.put("category", c.get("category"));
            row.put("submissionCount", subs.size());
            row.put("avgScore", calcAvgScore(subs));
            row.put("passRate", calcPassRate(subs));
            result.add(row);
        }
        return result;
    }
}
