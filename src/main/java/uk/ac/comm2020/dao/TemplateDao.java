package uk.ac.comm2020.dao;

import uk.ac.comm2020.db.Database;
import uk.ac.comm2020.model.Template;
import uk.ac.comm2020.util.JsonUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// DAO for template table.
// Just do DB stuff here, no extra logic.
public class TemplateDao {
    private final Database database;

    // keep db helper here
    public TemplateDao(Database database) {
        this.database = database;
    }

    // Find templates by category.
    public List<Template> findByCategory(String category) throws SQLException {
        String sql = "SELECT template_id, category, required_fields, optional_fields, rule_set_id " +
                "FROM template WHERE category = ?";
        List<Template> templates = new ArrayList<>();

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, category);

            // read list from DB
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    templates.add(mapTemplate(rs));
                }
            }
        }

        return templates;
    }

    // Get required fields list by template id.
    // Return empty if not found.
    public Optional<List<String>> findRequiredFieldsById(long templateId) throws SQLException {
        String sql = "SELECT required_fields FROM template WHERE template_id = ?";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, templateId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    String json = rs.getString("required_fields");
                    return Optional.of(JsonUtil.parseStringList(json));
                }
            }
        }

        return Optional.empty();
    }

    // Find one template by id.
    public Optional<Template> findById(long templateId) throws SQLException {
        String sql = "SELECT template_id, category, required_fields, optional_fields, rule_set_id " +
                "FROM template WHERE template_id = ?";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, templateId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapTemplate(rs));
                }
            }
        }

        return Optional.empty();
    }

    // Check template id exist or not.
    public boolean exists(long templateId) throws SQLException {
        String sql = "SELECT 1 FROM template WHERE template_id = ?";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, templateId);

            // if any row, then it exists
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Map one DB row -> Template object.
    private Template mapTemplate(ResultSet rs) throws SQLException {
        long templateId = rs.getLong("template_id");
        String category = rs.getString("category");
        String required = rs.getString("required_fields");
        String optional = rs.getString("optional_fields");
        int ruleSetId = rs.getInt("rule_set_id");

        return new Template(
                templateId,
                category,
                JsonUtil.parseStringList(required),
                JsonUtil.parseStringList(optional),
                ruleSetId
        );
    }
}
