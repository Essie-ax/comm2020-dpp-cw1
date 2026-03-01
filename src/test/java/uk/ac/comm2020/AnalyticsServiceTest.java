package uk.ac.comm2020;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.comm2020.dao.InMemoryChallengeDao;
import uk.ac.comm2020.dao.InMemorySubmissionDao;
import uk.ac.comm2020.service.AnalyticsService;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AnalyticsServiceTest {

    private AnalyticsService service;
    private InMemorySubmissionDao submissionDao;

    @BeforeEach
    void setUp() {
        submissionDao = new InMemorySubmissionDao();
        service = new AnalyticsService(submissionDao, new InMemoryChallengeDao());
    }

    // --- Test 1: getAnalytics returns all required keys ---
    @Test
    void getAnalyticsReturnsRequiredKeys() {
        Map<String, Object> result = service.getAnalytics();
        assertTrue(result.containsKey("totalChallenges"));
        assertTrue(result.containsKey("totalSubmissions"));
        assertTrue(result.containsKey("avgScore"));
        assertTrue(result.containsKey("passRate"));
        assertTrue(result.containsKey("scoreDistribution"));
        assertTrue(result.containsKey("challenges"));
    }

    // --- Test 2: No submissions -> avgScore and passRate are 0 ---
    @Test
    void noSubmissionsGivesZeroAvgAndPassRate() {
        Map<String, Object> result = service.getAnalytics();
        assertEquals(0.0, result.get("avgScore"));
        assertEquals(0.0, result.get("passRate"));
        assertEquals(0, result.get("totalSubmissions"));
    }

    // --- Test 3: avgScore calculated correctly ---
    @Test
    void avgScoreCalculatedCorrectly() {
        submissionDao.createSubmission(1, 1, 1, 80, "PASS");
        submissionDao.createSubmission(1, 2, 2, 60, "FAIL");

        Map<String, Object> result = service.getAnalytics();
        // (80 + 60) / 2 = 70.0
        assertEquals(70.0, result.get("avgScore"));
    }

    // --- Test 4: passRate calculated correctly ---
    @Test
    void passRateCalculatedCorrectly() {
        submissionDao.createSubmission(1, 1, 1, 90, "PASS");
        submissionDao.createSubmission(1, 2, 2, 40, "FAIL");
        submissionDao.createSubmission(1, 3, 3, 75, "PASS");
        submissionDao.createSubmission(1, 4, 4, 30, "FAIL");

        Map<String, Object> result = service.getAnalytics();
        // 2 out of 4 passed = 50.0%
        assertEquals(50.0, result.get("passRate"));
    }

    // --- Test 5: scoreDistribution has 4 buckets ---
    @Test
    void scoreDistributionHasFourBuckets() {
        submissionDao.createSubmission(1, 1, 1, 10, "FAIL");  // 0-24
        submissionDao.createSubmission(1, 2, 2, 35, "FAIL");  // 25-49
        submissionDao.createSubmission(1, 3, 3, 60, "PASS");  // 50-74
        submissionDao.createSubmission(1, 4, 4, 85, "PASS");  // 75-100

        Map<String, Object> result = service.getAnalytics();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> dist = (List<Map<String, Object>>) result.get("scoreDistribution");

        assertEquals(4, dist.size());
        // Each bucket should have exactly 1 submission
        for (Map<String, Object> bucket : dist) {
            assertEquals(1, bucket.get("count"));
        }
    }

    // --- Test 6: challenges breakdown lists at least one challenge ---
    @Test
    void challengeBreakdownListsSeededChallenge() {
        Map<String, Object> result = service.getAnalytics();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> challenges = (List<Map<String, Object>>) result.get("challenges");
        // InMemoryChallengeDao seeds one challenge
        assertTrue(challenges.size() >= 1);
        Map<String, Object> first = challenges.get(0);
        assertTrue(first.containsKey("challengeId"));
        assertTrue(first.containsKey("title"));
        assertTrue(first.containsKey("submissionCount"));
    }
}
