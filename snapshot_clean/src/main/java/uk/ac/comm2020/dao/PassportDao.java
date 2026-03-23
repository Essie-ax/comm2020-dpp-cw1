package uk.ac.comm2020.dao;

import uk.ac.comm2020.db.Database;
import uk.ac.comm2020.model.Passport;
import uk.ac.comm2020.util.JsonUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.Optional;

// DAO for passport table.
// Only do DB read/write here, no business rule.
public class PassportDao implements PassportRepository {
    private final Database database;

    // keep db helper here
    public PassportDao(Database database) {
        this.database = database;
    }

    // Make a new draft passport row.
    // fields start as {}, scores start as 0, status is DRAFT.
    public Passport createDraft(long productId, long templateId, long createdBy) throws SQLException {
        String sql = "INSERT INTO passport (product_id, template_id, fields, completeness_score, confidence_score, status, created_by) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, productId);
            statement.setLong(2, templateId);
            statement.setString(3, "{}");

            // keep 4 decimals, so score look stable
            statement.setBigDecimal(4, BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
            statement.setBigDecimal(5, BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));

            statement.setString(6, "DRAFT");
            statement.setLong(7, createdBy);

            int affected = statement.executeUpdate();
            if (affected > 0) {
                // normal way: read auto id from driver
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) {
                        long passportId = keys.getLong(1);
                        return new Passport(passportId, productId, templateId, "DRAFT", JsonUtil.parseObject("{}"), 0.0, 0.0);
                    }
                }

                // fallback way: some db/driver may not return keys well
                try (PreparedStatement fallback = connection.prepareStatement("SELECT LAST_INSERT_ID()");
                     ResultSet rs = fallback.executeQuery()) {
                    if (rs.next()) {
                        long passportId = rs.getLong(1);
                        if (passportId > 0) {
                            return new Passport(passportId, productId, templateId, "DRAFT", JsonUtil.parseObject("{}"), 0.0, 0.0);
                        }
                    }
                }
            }
        }

        throw new SQLException("Failed to create passport");
    }

    // Find passport by id. If not found, return empty.
    public Optional<Passport> findById(long passportId) throws SQLException {
        String sql = "SELECT passport_id, product_id, template_id, status, fields, completeness_score, confidence_score " +
                "FROM passport WHERE passport_id = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, passportId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapPassport(rs));
                }
            }
        }
        return Optional.empty();
    }

    // Update fields json and scores for one passport.
    public void updateFields(long passportId, String fieldsJson, double completenessScore, double confidenceScore) throws SQLException {
        String sql = "UPDATE passport SET fields = ?, completeness_score = ?, confidence_score = ? WHERE passport_id = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, fieldsJson);

            // store with 4 decimals, same as insert
            statement.setBigDecimal(2, BigDecimal.valueOf(completenessScore).setScale(4, RoundingMode.HALF_UP));
            statement.setBigDecimal(3, BigDecimal.valueOf(confidenceScore).setScale(4, RoundingMode.HALF_UP));

            statement.setLong(4, passportId);

            statement.executeUpdate();
        }
    }

    // Map one DB row -> Passport object.
    private Passport mapPassport(ResultSet rs) throws SQLException {
        return new Passport(
                rs.getLong("passport_id"),
                rs.getLong("product_id"),
                rs.getLong("template_id"),
                rs.getString("status"),
                JsonUtil.parseObject(rs.getString("fields")),
                rs.getDouble("completeness_score"),
                rs.getDouble("confidence_score")
        );
    }
}
