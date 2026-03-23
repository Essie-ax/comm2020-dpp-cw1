package uk.ac.comm2020.dao;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/** In-memory challenge store with one seeded demo challenge. */
public class InMemoryChallengeDao implements ChallengeDao {

    private final Map<Long, Map<String, Object>> store = new LinkedHashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);

    public InMemoryChallengeDao() {
        Map<String, Object> demo = new LinkedHashMap<>();
        demo.put("challengeId", 1L);
        demo.put("title", "Battery Compliance Sprint");
        demo.put("category", "Battery");
        demo.put("constraints", "{\"minCompleteness\":0.8"
                + ",\"requiredFields\":[\"name\",\"brand\",\"origin\",\"chemistry\"]"
                + ",\"requiredEvidenceTypes\":[\"CERTIFICATE\"]}");
        demo.put("scoringRules", "{\"base\":100,\"bonusEvidence\":20,\"bonusAllFields\":10}");
        demo.put("startDate", "2026-02-01");
        demo.put("endDate", "2026-03-01");
        demo.put("createdBy", 2L);
        demo.put("createdAt", "2026-02-01T00:00:00");
        store.put(1L, demo);
        idGen.set(2);
    }

    @Override
    public long createChallenge(String title, String category, String constraintsJson,
                                String scoringRulesJson, String startDate, String endDate, long createdBy) {
        long id = idGen.getAndIncrement();
        Map<String, Object> c = new LinkedHashMap<>();
        c.put("challengeId", id);
        c.put("title", title);
        c.put("category", category);
        c.put("constraints", constraintsJson);
        c.put("scoringRules", scoringRulesJson);
        c.put("startDate", startDate);
        c.put("endDate", endDate);
        c.put("createdBy", createdBy);
        c.put("createdAt", LocalDateTime.now().toString());
        store.put(id, c);
        return id;
    }

    @Override
    public List<Map<String, Object>> getChallenges(String category) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> c : store.values()) {
            if (category == null || category.isBlank() || category.equals(c.get("category"))) {
                result.add(c);
            }
        }
        Collections.reverse(result); // newest first
        return result;
    }

    @Override
    public Map<String, Object> getChallengeById(long id) {
        return store.get(id);
    }
}
