package uk.ac.comm2020.dao;

import uk.ac.comm2020.db.Database;
import uk.ac.comm2020.model.Evidence;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// DAO for evidence table.
// Only DB read/write here, no business rule.
public class EvidenceDao implements EvidenceRepository {

    private final Database database;

    public EvidenceDao(Database database) {
        this.database = database;
    }

    // Save one evidence record.
    public void save(Evidence evidence) throws SQLException {
        String sql = "INSERT INTO evidence (passport_id, field_key, type, issuer, summary) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, evidence.getPassportId());
            statement.setString(2, evidence.getFieldKey());
            statement.setString(3, evidence.getType());
            statement.setString(4, evidence.getIssuer());
            statement.setString(5, evidence.getSummary());

            statement.executeUpdate();
        }
    }

    // Find all evidence for one passport.
    public List<Evidence> findByPassportId(long passportId) throws SQLException {
        String sql = "SELECT evidence_id, passport_id, field_key, type, issuer, summary " +
                     "FROM evidence WHERE passport_id = ?";

        List<Evidence> result = new ArrayList<>();

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, passportId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.add(mapEvidence(rs));
                }
            }
        }

        return result;
    }

    // Map one DB row -> Evidence object.
    private Evidence mapEvidence(ResultSet rs) throws SQLException {
        return new Evidence(
                rs.getLong("evidence_id"),
                rs.getLong("passport_id"),
                rs.getString("field_key"),
                rs.getString("type"),
                rs.getString("issuer"),
                rs.getString("summary")
        );
    }
}
