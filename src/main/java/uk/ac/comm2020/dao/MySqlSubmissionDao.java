package uk.ac.comm2020.dao;

import uk.ac.comm2020.util.Db;

import java.sql.*;
import java.util.*;

/** MySQL-backed submission DAO. Uses the submission table from 001_init.sql. */
public class MySqlSubmissionDao implements SubmissionDao {

    @Override
    public long createSubmission(long challengeId, long passportId, long submittedBy, int score, String outcome) {
        String sql = "INSERT INTO submission (challenge_id, passport_id, submitted_by, score, outcome) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = Db.getConnection()) {
            if (c == null) return -1;
            try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, challengeId);
                ps.setLong(2, passportId);
                ps.setLong(3, submittedBy);
                ps.setInt(4, score);
                ps.setString(5, outcome);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("createSubmission error: " + e.getMessage());
        }
        return -1;
    }

    @Override
    public Map<String, Object> getSubmissionById(long id) {
        String sql = "SELECT * FROM submission WHERE submission_id = ?";
        try (Connection c = Db.getConnection()) {
            if (c == null) return null;
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setLong(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rowToMap(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("getSubmissionById error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Map<String, Object>> getSubmissionsByChallenge(long challengeId) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT * FROM submission WHERE challenge_id = ? ORDER BY score DESC LIMIT 100";
        try (Connection c = Db.getConnection()) {
            if (c == null) return list;
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setLong(1, challengeId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(rowToMap(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("getSubmissionsByChallenge error: " + e.getMessage());
        }
        return list;
    }

    private Map<String, Object> rowToMap(ResultSet rs) throws SQLException {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("submissionId", rs.getLong("submission_id"));
        m.put("challengeId", rs.getLong("challenge_id"));
        m.put("passportId", rs.getLong("passport_id"));
        m.put("submittedBy", rs.getLong("submitted_by"));
        m.put("score", rs.getInt("score"));
        m.put("outcome", rs.getString("outcome"));
        m.put("submittedAt", rs.getString("submitted_at"));
        return m;
    }
}
