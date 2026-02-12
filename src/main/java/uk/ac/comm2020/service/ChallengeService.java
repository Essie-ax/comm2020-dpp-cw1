package uk.ac.comm2020.service;

import uk.ac.comm2020.dao.ChallengeDao;
import uk.ac.comm2020.model.Role;
import uk.ac.comm2020.service.SessionService.Session;
import uk.ac.comm2020.util.ApiResponse;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ChallengeService {

    private final ChallengeDao challengeDao;
    private final SessionService sessionService;

    public ChallengeService(ChallengeDao challengeDao, SessionService sessionService) {
        this.challengeDao = challengeDao;
        this.sessionService = sessionService;
    }

    /** Create a challenge. Only GAME_KEEPER role allowed. */
    public ApiResponse createChallenge(String token, Map<String, String> body) {
        Session session = sessionService.validateToken(token);
        if (session == null) {
            return ApiResponse.error("UNAUTHORIZED", "Missing or invalid token");
        }
        if (session.role != Role.GAME_KEEPER) {
            return ApiResponse.error("FORBIDDEN", "Only GameKeeper can create challenges");
        }

        String title = body.getOrDefault("title", "").trim();
        String category = body.getOrDefault("category", "").trim();
        String startDate = body.getOrDefault("startDate", "").trim();
        String endDate = body.getOrDefault("endDate", "").trim();

        if (title.isEmpty() || category.isEmpty()) {
            return ApiResponse.error("BAD_REQUEST", "title and category are required");
        }
        if (startDate.isEmpty() || endDate.isEmpty()) {
            return ApiResponse.error("BAD_REQUEST", "startDate and endDate are required");
        }

        // Build constraints JSON from flat fields (simplified for CW1)
        String minComp = body.getOrDefault("minCompleteness", "0.8");
        String reqTypes = body.getOrDefault("requiredEvidenceTypes", "CERTIFICATE");
        String constraintsJson = "{\"minCompleteness\":" + minComp
                + ",\"requiredEvidenceTypes\":[\"" + reqTypes + "\"]}";

        // Build scoring rules JSON
        String baseScore = body.getOrDefault("baseScore", "100");
        String bonus = body.getOrDefault("bonusEvidence", "20");
        String scoringJson = "{\"base\":" + baseScore + ",\"bonusEvidence\":" + bonus + "}";

        long id = challengeDao.createChallenge(title, category, constraintsJson, scoringJson,
                startDate, endDate, session.userId);

        if (id <= 0) {
            return ApiResponse.error("INTERNAL_ERROR", "Failed to create challenge");
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("challengeId", id);
        return ApiResponse.ok(data);
    }

    /** List challenges, optionally filtered by category. */
    public ApiResponse getChallenges(String category) {
        List<Map<String, Object>> list = challengeDao.getChallenges(category);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("challenges", list.toArray(new Object[0]));
        return ApiResponse.ok(data);
    }

    /** Get single challenge by id. */
    public ApiResponse getChallengeById(long id) {
        Map<String, Object> challenge = challengeDao.getChallengeById(id);
        if (challenge == null) {
            return ApiResponse.error("NOT_FOUND", "Challenge not found: " + id);
        }
        return ApiResponse.ok(challenge);
    }
}
