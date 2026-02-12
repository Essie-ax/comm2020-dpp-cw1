package uk.ac.comm2020.dao;

import java.util.List;
import java.util.Map;

/** DAO interface for submission CRUD. */
public interface SubmissionDao {

    long createSubmission(long challengeId, long passportId, long submittedBy, int score, String outcome);

    Map<String, Object> getSubmissionById(long id);

    List<Map<String, Object>> getSubmissionsByChallenge(long challengeId);
}
