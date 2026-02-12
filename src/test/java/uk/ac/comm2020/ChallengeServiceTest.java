package uk.ac.comm2020;

import org.junit.jupiter.api.Test;
import uk.ac.comm2020.dao.InMemoryChallengeDao;
import uk.ac.comm2020.dao.InMemoryUserDao;
import uk.ac.comm2020.service.ChallengeService;
import uk.ac.comm2020.service.SessionService;
import uk.ac.comm2020.util.ApiResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ChallengeServiceTest {

    /** Helper: create service with in-memory DAOs. */
    private ChallengeService createService(SessionService sessionService) {
        return new ChallengeService(new InMemoryChallengeDao(), sessionService);
    }

    /** Helper: login as gamekeeper1, return token. */
    private String loginAsGameKeeper(SessionService ss) {
        ApiResponse res = ss.authenticate("gamekeeper1", "password");
        return (String) res.getData().get("token");
    }

    // --- Test 1: GameKeeper can create a challenge ---
    @Test
    void gamekeeperCanCreateChallenge() {
        SessionService ss = new SessionService(new InMemoryUserDao());
        ChallengeService service = createService(ss);
        String token = loginAsGameKeeper(ss);

        Map<String, String> body = new HashMap<>();
        body.put("title", "Test Challenge");
        body.put("category", "Battery");
        body.put("startDate", "2026-03-01");
        body.put("endDate", "2026-04-01");

        ApiResponse res = service.createChallenge(token, body);
        assertTrue(res.isSuccess());
        assertNotNull(res.getData().get("challengeId"));
    }

    // --- Test 2: Create fails when title is empty ---
    @Test
    void createFailsWithMissingTitle() {
        SessionService ss = new SessionService(new InMemoryUserDao());
        ChallengeService service = createService(ss);
        String token = loginAsGameKeeper(ss);

        Map<String, String> body = new HashMap<>();
        body.put("title", "");
        body.put("category", "Battery");
        body.put("startDate", "2026-03-01");
        body.put("endDate", "2026-04-01");

        ApiResponse res = service.createChallenge(token, body);
        assertFalse(res.isSuccess());
        assertEquals(400, res.getStatus());
    }

    // --- Test 3: List returns seeded + created challenges ---
    @Test
    void getChallengesReturnsList() {
        SessionService ss = new SessionService(new InMemoryUserDao());
        ChallengeService service = createService(ss);

        ApiResponse res = service.getChallenges(null);
        assertTrue(res.isSuccess());
        List<?> challenges = (List<?>) res.getData().get("challenges");
        assertTrue(challenges.size() >= 1); // at least the seeded demo
    }

    // --- Test 4: Detail for non-existent id returns NOT_FOUND ---
    @Test
    void getChallengeByIdReturnsNotFound() {
        SessionService ss = new SessionService(new InMemoryUserDao());
        ChallengeService service = createService(ss);

        ApiResponse res = service.getChallengeById(999);
        assertFalse(res.isSuccess());
        assertEquals(404, res.getStatus());
    }

    // --- Test 5: Player cannot create a challenge ---
    @Test
    void playerCannotCreateChallenge() {
        SessionService ss = new SessionService(new InMemoryUserDao());
        ChallengeService service = createService(ss);

        ApiResponse loginRes = ss.authenticate("player1", "password");
        String token = (String) loginRes.getData().get("token");

        Map<String, String> body = new HashMap<>();
        body.put("title", "Should Fail");
        body.put("category", "Battery");
        body.put("startDate", "2026-03-01");
        body.put("endDate", "2026-04-01");

        ApiResponse res = service.createChallenge(token, body);
        assertFalse(res.isSuccess());
        assertEquals(403, res.getStatus());
    }
}
