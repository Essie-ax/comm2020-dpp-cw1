package uk.ac.comm2020.service;

import uk.ac.comm2020.dao.ChallengeDao;
import uk.ac.comm2020.dao.SubmissionDao;
import uk.ac.comm2020.model.Role;
import uk.ac.comm2020.service.SessionService.Session;
import uk.ac.comm2020.util.ApiResponse;

import java.util.LinkedHashMap;
import java.util.Map;

public class SubmissionService {

    private final SubmissionDao submissionDao;
    private final ChallengeDao challengeDao;
    private final SessionService sessionService;

    public SubmissionService(SubmissionDao submissionDao, ChallengeDao challengeDao, SessionService sessionService) {
        this.submissionDao = submissionDao;
        this.challengeDao = challengeDao;
        this.sessionService = sessionService;
    }

    /** Submit a passport to a challenge. Player role required. */
    public ApiResponse submit(String token, long challengeId, Map<String, String> body) {
        // Auth check
        Session session = sessionService.validateToken(token);
        if (session == null) {
            return ApiResponse.error("UNAUTHORIZED", "Missing or invalid token");
        }
        if (session.role != Role.PLAYER) {
            return ApiResponse.error("FORBIDDEN", "Only Player can submit to challenges");
        }

        // Parse passportId
        String passportIdStr = body.getOrDefault("passportId", "").trim();
        long passportId;
        try {
            passportId = Long.parseLong(passportIdStr);
        } catch (NumberFormatException e) {
            return ApiResponse.error("BAD_REQUEST", "passportId is required and must be a number");
        }
        if (passportId <= 0) {
            return ApiResponse.error("BAD_REQUEST", "passportId must be positive");
        }

        // Check challenge exists
        Map<String, Object> challenge = challengeDao.getChallengeById(challengeId);
        if (challenge == null) {
            return ApiResponse.error("NOT_FOUND", "Challenge not found: " + challengeId);
        }

        // Mock scoring: deterministic based on passportId
        double completeness = getMockCompleteness(passportId);
        double evidenceCoverage = getMockEvidenceCoverage(passportId);
        int score = (int) (0.7 * completeness + 0.3 * evidenceCoverage);

        // PASS/FAIL based on challenge minCompleteness
        double threshold = extractMinCompleteness(challenge) * 100;
        String outcome = (score >= threshold) ? "PASS" : "FAIL";

        // Generate feedback
        String[] feedback = generateFeedback(outcome, score, (int) threshold, completeness, evidenceCoverage);

        // Save to DB
        long submissionId = submissionDao.createSubmission(challengeId, passportId, session.userId, score, outcome);
        if (submissionId <= 0) {
            return ApiResponse.error("INTERNAL_ERROR", "Failed to save submission");
        }

        // Build response
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("submissionId", submissionId);
        data.put("challengeId", challengeId);
        data.put("passportId", passportId);
        data.put("score", score);
        data.put("outcome", outcome);
        data.put("feedback", feedback);
        return ApiResponse.ok(data);
    }

    // --- Mock data source (replace with real DAO when module B/C ready) ---

    /** Mock completeness score (0-100) based on passportId. */
    private double getMockCompleteness(long passportId) {
        if (passportId == 1) return 85.0;  // seed passport: mostly complete
        if (passportId == 2) return 45.0;  // partial
        return 30.0;                        // low
    }

    /** Mock evidence coverage (0-100) based on passportId. */
    private double getMockEvidenceCoverage(long passportId) {
        if (passportId == 1) return 70.0;  // seed passport has one evidence
        if (passportId == 2) return 20.0;
        return 10.0;
    }

    /** Extract minCompleteness from challenge constraints JSON string. */
    private double extractMinCompleteness(Map<String, Object> challenge) {
        String json = (String) challenge.get("constraints");
        if (json == null) return 0.8;
        String key = "\"minCompleteness\":";
        int idx = json.indexOf(key);
        if (idx < 0) return 0.8;
        int start = idx + key.length();
        // skip whitespace
        while (start < json.length() && json.charAt(start) == ' ') start++;
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '.')) end++;
        try {
            return Double.parseDouble(json.substring(start, end));
        } catch (NumberFormatException e) {
            return 0.8;
        }
    }

    /** Generate readable feedback based on outcome. */
    private String[] generateFeedback(String outcome, int score, int threshold,
                                      double completeness, double evidence) {
        if ("PASS".equals(outcome)) {
            return new String[]{
                "Score " + score + " meets threshold " + threshold,
                "Completeness: " + (int) completeness + "%, Evidence: " + (int) evidence + "%",
                "Good submission. Keep evidence quality stable."
            };
        } else {
            return new String[]{
                "Score " + score + " below threshold " + threshold,
                "Completeness: " + (int) completeness + "%, Evidence: " + (int) evidence + "%",
                "Add missing required fields and evidence to improve score."
            };
        }
    }
}
