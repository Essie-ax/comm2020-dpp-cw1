package uk.ac.comm2020.dao;

import uk.ac.comm2020.util.Db;

import java.sql.*;
import java.util.*;

/** MySQL-backed challenge DAO. Reads/writes the challenge table from 001_init.sql. */
public class MySqlChallengeDao implements ChallengeDao {

    @Override
    public long createChallenge(String title, String category, String constraintsJson,
                                String scoringRulesJson, String startDate, String endDate, long createdBy) {
        String sql = "INSERT INTO challenge (title, category, constraints, scoring_rules, start_date, end_date, created_by) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = Db.getConnection()) {
            if (c == null) return -1;
            try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, title);
                ps.setString(2, category);
                ps.setString(3, constraintsJson);
                ps.setString(4, scoringRulesJson);
                ps.setString(5, startDate);
                ps.setString(6, endDate);
                ps.setLong(7, createdBy);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("createChallenge error: " + e.getMessage());
        }
        return -1;
    }

    @Override
    public List<Map<String, Object>> getChallenges(String category) {
        List<Map<String, Object>> list = new ArrayList<>();
        boolean hasCategory = category != null && !category.isBlank();
        String sql = hasCategory
                ? "SELECT * FROM challenge WHERE category = ? ORDER BY created_at DESC LIMIT 100"
                : "SELECT * FROM challenge ORDER BY created_at DESC LIMIT 100";
        try (Connection c = Db.getConnection()) {
            if (c == null) return list;
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                if (hasCategory) ps.setString(1, category);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(rowToMap(rs));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("getChallenges error: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Map<String, Object> getChallengeById(long id) {
        String sql = "SELECT * FROM challenge WHERE challenge_id = ?";
        try (Connection c = Db.getConnection()) {
            if (c == null) return null;
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setLong(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rowToMap(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("getChallengeById error: " + e.getMessage());
        }
        return null;
    }

    private Map<String, Object> rowToMap(ResultSet rs) throws SQLException {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("challengeId", rs.getLong("challenge_id"));
        m.put("title", rs.getString("title"));
        m.put("category", rs.getString("category"));
        m.put("constraints", rs.getString("constraints"));
        m.put("scoringRules", rs.getString("scoring_rules"));
        m.put("startDate", rs.getString("start_date"));
        m.put("endDate", rs.getString("end_date"));
        m.put("createdBy", rs.getLong("created_by"));
        m.put("createdAt", rs.getString("created_at"));
        return m;
    }
}
