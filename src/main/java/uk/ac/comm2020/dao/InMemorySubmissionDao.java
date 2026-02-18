package uk.ac.comm2020.dao;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/** In-memory submission store. */
public class InMemorySubmissionDao implements SubmissionDao {

    private final Map<Long, Map<String, Object>> store = new LinkedHashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);

    @Override
    public long createSubmission(long challengeId, long passportId, long submittedBy, int score, String outcome) {
        long id = idGen.getAndIncrement();
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("submissionId", id);
        s.put("challengeId", challengeId);
        s.put("passportId", passportId);
        s.put("submittedBy", submittedBy);
        s.put("score", score);
        s.put("outcome", outcome);
        s.put("submittedAt", LocalDateTime.now().toString());
        store.put(id, s);
        return id;
    }

    @Override
    public Map<String, Object> getSubmissionById(long id) {
        return store.get(id);
    }

    @Override
    public List<Map<String, Object>> getSubmissionsByChallenge(long challengeId) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> s : store.values()) {
            if (Long.valueOf(challengeId).equals(s.get("challengeId"))) {
                result.add(s);
            }
        }
        result.sort((a, b) -> Integer.compare((int) b.get("score"), (int) a.get("score")));
        return result;
    }
}
