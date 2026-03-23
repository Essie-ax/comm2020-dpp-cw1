package uk.ac.comm2020;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.comm2020.dao.InMemoryChallengeDao;
import uk.ac.comm2020.dao.InMemorySubmissionDao;
import uk.ac.comm2020.dao.InMemoryUserDao;
import uk.ac.comm2020.service.SessionService;
import uk.ac.comm2020.service.SubmissionService;
import uk.ac.comm2020.util.ApiResponse;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SubmissionServiceTest {

    private SubmissionService service;
    private SessionService sessionService;
    private String playerToken;

    @BeforeEach
    void setUp() {
        InMemoryUserDao userDao = new InMemoryUserDao();
        InMemoryChallengeDao challengeDao = new InMemoryChallengeDao();
        InMemorySubmissionDao submissionDao = new InMemorySubmissionDao();
        sessionService = new SessionService(userDao);
        service = new SubmissionService(submissionDao, challengeDao, sessionService);

        // Login as player1
        ApiResponse login = sessionService.authenticate("player1", "password");
        playerToken = (String) login.getData().get("token");
    }

    // Test 1: Submit success — returns score + outcome + feedback
    @Test
    void submitSuccessReturnsScoreAndOutcome() {
        Map<String, String> body = new HashMap<>();
        body.put("passportId", "1");

        // InMemoryChallengeDao seeds one challenge with id=1
        ApiResponse res = service.submit(playerToken, 1, body);
        assertTrue(res.isSuccess());

        Map<String, Object> data = res.getData();
        assertNotNull(data.get("submissionId"));
        assertEquals(1L, data.get("challengeId"));
        assertNotNull(data.get("score"));
        assertNotNull(data.get("outcome"));
        assertNotNull(data.get("feedback"));
    }

    // Test 2: Challenge not found — returns 404
    @Test
    void submitToNonExistentChallengeReturns404() {
        Map<String, String> body = new HashMap<>();
        body.put("passportId", "1");

        ApiResponse res = service.submit(playerToken, 999, body);
        assertFalse(res.isSuccess());
        assertEquals(404, res.getStatus());
    }

    // Test 3: Invalid passportId — returns 400
    @Test
    void submitWithInvalidPassportIdReturns400() {
        Map<String, String> body = new HashMap<>();
        body.put("passportId", "0");

        ApiResponse res = service.submit(playerToken, 1, body);
        assertFalse(res.isSuccess());
        assertEquals(400, res.getStatus());
    }

    // Test 4: High-scoring passport (id=1) should PASS
    @Test
    void highScoringPassportGetsPass() {
        Map<String, String> body = new HashMap<>();
        body.put("passportId", "1"); // mock: completeness=85, evidence=70

        ApiResponse res = service.submit(playerToken, 1, body);
        assertTrue(res.isSuccess());
        assertEquals("PASS", res.getData().get("outcome"));
        int score = (int) res.getData().get("score");
        assertTrue(score >= 70, "Score should be >= 70 for passport 1");
    }

    // Test 5: Low-scoring passport (id=99) should FAIL
    @Test
    void lowScoringPassportGetsFail() {
        Map<String, String> body = new HashMap<>();
        body.put("passportId", "99"); // mock: completeness=30, evidence=10

        ApiResponse res = service.submit(playerToken, 1, body);
        assertTrue(res.isSuccess());
        assertEquals("FAIL", res.getData().get("outcome"));
        int score = (int) res.getData().get("score");
        assertTrue(score < 50, "Score should be low for unknown passport");
    }

    // Test 6: GameKeeper cannot submit
    @Test
    void gamekeeperCannotSubmit() {
        ApiResponse gkLogin = sessionService.authenticate("gamekeeper1", "password");
        String gkToken = (String) gkLogin.getData().get("token");

        Map<String, String> body = new HashMap<>();
        body.put("passportId", "1");

        ApiResponse res = service.submit(gkToken, 1, body);
        assertFalse(res.isSuccess());
        assertEquals(403, res.getStatus());
    }
}
