package uk.ac.comm2020.service;

import uk.ac.comm2020.dao.ChallengeDao;
import uk.ac.comm2020.dao.EvidenceDao;
import uk.ac.comm2020.dao.PassportDao;
import uk.ac.comm2020.dao.SubmissionDao;
import uk.ac.comm2020.model.Evidence;
import uk.ac.comm2020.model.Passport;
import uk.ac.comm2020.model.Role;
import uk.ac.comm2020.service.SessionService.Session;
import uk.ac.comm2020.util.ApiResponse;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SubmissionService {

    private final SubmissionDao submissionDao;
    private final ChallengeDao challengeDao;
    private final SessionService sessionService;
    private final PassportDao passportDao;    // nullable - null means use mock
    private final EvidenceDao evidenceDao;    // nullable - null means use mock

    // Full constructor with real Passport/Evidence DAOs
    public SubmissionService(SubmissionDao submissionDao, ChallengeDao challengeDao,
                             SessionService sessionService, PassportDao passportDao, EvidenceDao evidenceDao) {
        this.submissionDao = submissionDao;
        this.challengeDao = challengeDao;
        this.sessionService = sessionService;
        this.passportDao = passportDao;
        this.evidenceDao = evidenceDao;
    }

    // Backward-compatible constructor (uses mock scoring, for tests)
    public SubmissionService(SubmissionDao submissionDao, ChallengeDao challengeDao, SessionService sessionService) {
        this(submissionDao, challengeDao, sessionService, null, null);
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

        // Get completeness and evidence scores (real DB or mock fallback)
        double completeness = getCompleteness(passportId);
        double evidenceCoverage = getEvidenceCoverage(passportId);
        int score = (int) (0.7 * completeness + 0.3 * evidenceCoverage);

        // PASS/FAIL based on challenge minCompleteness
        double threshold = extractMinCompleteness(challenge) * 100;
        String outcome = (score >= threshold) ? "PASS" : "FAIL";

        // Indicate data source in feedback
        boolean usingReal = (passportDao != null);
        String[] feedback = generateFeedback(outcome, score, (int) threshold, completeness, evidenceCoverage, usingReal);

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
        data.put("dataSource", usingReal ? "real" : "mock");
        return ApiResponse.ok(data);
    }

    // --- Scoring: try real DB first, fallback to mock ---

    /** Get completeness score (0-100) from real passport or mock. */
    private double getCompleteness(long passportId) {
        if (passportDao != null) {
            try {
                Optional<Passport> passport = passportDao.findById(passportId);
                if (passport.isPresent()) {
                    // DB stores 0.0~1.0, convert to 0~100
                    return passport.get().getCompletenessScore() * 100;
                }
            } catch (SQLException e) {
                System.err.println("Failed to query passport " + passportId + ", using mock: " + e.getMessage());
            }
        }
        return getMockCompleteness(passportId);
    }

    /** Get evidence coverage (0-100) from real evidence or mock. */
    private double getEvidenceCoverage(long passportId) {
        if (evidenceDao != null && passportDao != null) {
            try {
                Optional<Passport> passport = passportDao.findById(passportId);
                if (passport.isPresent()) {
                    // Use confidence score from passport as evidence coverage
                    double confidence = passport.get().getConfidenceScore() * 100;
                    // Also count actual evidence records as a bonus factor
                    List<Evidence> evidences = evidenceDao.findByPassportId(passportId);
                    int evidenceCount = evidences.size();
                    // Blend confidence score with evidence count (max 5 pieces = 100%)
                    double countFactor = Math.min(evidenceCount * 20.0, 100.0);
                    return 0.6 * confidence + 0.4 * countFactor;
                }
            } catch (SQLException e) {
                System.err.println("Failed to query evidence for passport " + passportId + ", using mock: " + e.getMessage());
            }
        }
        return getMockEvidenceCoverage(passportId);
    }

    // --- Mock fallback (used when PassportDao is null or DB query fails) ---

    private double getMockCompleteness(long passportId) {
        if (passportId == 1) return 85.0;
        if (passportId == 2) return 45.0;
        return 30.0;
    }

    private double getMockEvidenceCoverage(long passportId) {
        if (passportId == 1) return 70.0;
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
        while (start < json.length() && json.charAt(start) == ' ') start++;
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '.')) end++;
        try {
            return Double.parseDouble(json.substring(start, end));
        } catch (NumberFormatException e) {
            return 0.8;
        }
    }

    /** Generate readable feedback. */
    private String[] generateFeedback(String outcome, int score, int threshold,
                                      double completeness, double evidence, boolean usingReal) {
        String source = usingReal ? " [real data]" : " [mock data]";
        if ("PASS".equals(outcome)) {
            return new String[]{
                "Score " + score + " meets threshold " + threshold + source,
                "Completeness: " + (int) completeness + "%, Evidence: " + (int) evidence + "%",
                "Good submission. Keep evidence quality stable."
            };
        } else {
            return new String[]{
                "Score " + score + " below threshold " + threshold + source,
                "Completeness: " + (int) completeness + "%, Evidence: " + (int) evidence + "%",
                "Add missing required fields and evidence to improve score."
            };
        }
    }
}
