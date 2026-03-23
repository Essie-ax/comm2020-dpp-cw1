package uk.ac.comm2020.dao;

import uk.ac.comm2020.db.Database;
import uk.ac.comm2020.model.Template;
import uk.ac.comm2020.util.JsonUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class TemplateDao {
    private final Database database;

    public TemplateDao(Database database) {
        this.database = database;
    }

    public Map<String, Object> getTemplates() throws SQLException {
        List<Template> templates = findAll();
        Map<String, Object> payload = new HashMap<>();
        payload.put("templates", templates);
        payload.put("count", templates.size());
        return payload;
    }

    public List<Template> findAll() throws SQLException {
        String sql = "SELECT template_id, category, required_fields, optional_fields, rule_set_id FROM template";
        List<Template> templates = new ArrayList<>();

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                templates.add(mapTemplate(rs));
            }
        }

        return templates;
    }

    public List<Template> findByCategory(String category) throws SQLException {
        String sql = "SELECT template_id, category, required_fields, optional_fields, rule_set_id " +
                "FROM template WHERE category = ?";
        List<Template> templates = new ArrayList<>();

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, category);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    templates.add(mapTemplate(rs));
                }
            }
        }

        return templates;
    }

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

    public boolean exists(long templateId) throws SQLException {
        String sql = "SELECT 1 FROM template WHERE template_id = ?";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, templateId);

            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

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
