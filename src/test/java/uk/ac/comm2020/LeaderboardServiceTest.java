package uk.ac.comm2020;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.comm2020.dao.InMemorySubmissionDao;
import uk.ac.comm2020.service.LeaderboardService;
import uk.ac.comm2020.util.ApiResponse;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class LeaderboardServiceTest {

    private InMemorySubmissionDao submissionDao;
    private LeaderboardService service;

    @BeforeEach
    void setUp() {
        submissionDao = new InMemorySubmissionDao();
        service = new LeaderboardService(submissionDao);
    }

    // Test 1: Normal leaderboard with correct ranking (score desc)
    @Test
    void leaderboardSortedByScoreDesc() {
        submissionDao.createSubmission(1, 100, 1, 60, "FAIL");
        submissionDao.createSubmission(1, 101, 2, 90, "PASS");
        submissionDao.createSubmission(1, 102, 3, 75, "PASS");

        ApiResponse res = service.getChallengeLeaderboard(1);
        assertTrue(res.isSuccess());

        Object[] entries = (Object[]) res.getData().get("entries");
        assertEquals(3, entries.length);

        // Rank 1 should be score=90, Rank 2 score=75, Rank 3 score=60
        Map<String, Object> first = asMap(entries[0]);
        Map<String, Object> second = asMap(entries[1]);
        Map<String, Object> third = asMap(entries[2]);

        assertEquals(1, first.get("rank"));
        assertEquals(90, first.get("score"));

        assertEquals(2, second.get("rank"));
        assertEquals(75, second.get("score"));

        assertEquals(3, third.get("rank"));
        assertEquals(60, third.get("score"));
    }

    // Test 2: Same score — earlier submission ranks higher
    @Test
    void sameScoreRankedBySubmitTime() {
        // Submit in order: player1 first, player2 second — both score 80
        submissionDao.createSubmission(1, 100, 1, 80, "PASS");
        submissionDao.createSubmission(1, 101, 2, 80, "PASS");

        ApiResponse res = service.getChallengeLeaderboard(1);
        Object[] entries = (Object[]) res.getData().get("entries");
        assertEquals(2, entries.length);

        Map<String, Object> first = asMap(entries[0]);
        Map<String, Object> second = asMap(entries[1]);

        assertEquals(1, first.get("rank"));
        assertEquals(2, second.get("rank"));
        // Player 1 submitted first, should be rank 1
        assertEquals(1L, first.get("playerId"));
        assertEquals(2L, second.get("playerId"));
    }

    // Test 3: Invalid challengeId returns 400
    @Test
    void invalidChallengeIdReturns400() {
        ApiResponse res = service.getChallengeLeaderboard(0);
        assertFalse(res.isSuccess());
        assertEquals(400, res.getStatus());

        ApiResponse res2 = service.getChallengeLeaderboard(-5);
        assertFalse(res2.isSuccess());
        assertEquals(400, res2.getStatus());
    }

    // Test 4: No submissions returns empty array
    @Test
    void noSubmissionsReturnsEmptyArray() {
        ApiResponse res = service.getChallengeLeaderboard(999);
        assertTrue(res.isSuccess());

        Object[] entries = (Object[]) res.getData().get("entries");
        assertEquals(0, entries.length);

        String msg = (String) res.getData().get("message");
        assertTrue(msg.contains("No submissions"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object obj) {
        return (Map<String, Object>) obj;
    }
}
