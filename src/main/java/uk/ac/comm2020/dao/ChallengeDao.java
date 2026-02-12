package uk.ac.comm2020.dao;

import java.util.List;
import java.util.Map;

/** DAO interface for challenge CRUD. */
public interface ChallengeDao {

    long createChallenge(String title, String category, String constraintsJson,
                         String scoringRulesJson, String startDate, String endDate, long createdBy);

    List<Map<String, Object>> getChallenges(String category);

    Map<String, Object> getChallengeById(long id);
}
