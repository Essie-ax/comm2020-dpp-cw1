package uk.ac.comm2020.dao;

import uk.ac.comm2020.util.Db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MySqlTemplateDao implements TemplateDao {

    @Override
    public Map<String, Object> getTemplates() {
        List<Map<String, Object>> list = new ArrayList<>();

        try (Connection c = Db.getConnection()) {
            if (c == null) return Map.of("templates", new Object[]{});

            String sql = "SELECT template_id, category, required_fields, optional_fields, rule_set_id, created_by, created_at FROM template ORDER BY template_id LIMIT 100";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> t = new HashMap<>();
                        long id = rs.getLong("template_id");
                        t.put("templateId", id);
                        String category = rs.getString("category");
                        if (category != null) t.put("category", category);
                        t.put("requiredFields", parseJsonStringArray(rs.getString("required_fields")));
                        t.put("optionalFields", parseJsonStringArray(rs.getString("optional_fields")));
                        t.put("ruleSetId", rs.getInt("rule_set_id"));
                        list.add(t);
                    }
                }
            }
        } catch (SQLException e) {
            // On any DB error return empty list so callers can still function
            return Map.of("templates", new Object[]{});
        }

        return Map.of("templates", list.toArray(new Object[0]));
    }

    /** Parse MySQL JSON array string like ["a","b"] into String[]. */
    private static String[] parseJsonStringArray(String json) {
        if (json == null || json.isBlank()) return new String[0];
        String s = json.trim();
        if (s.startsWith("[") && s.endsWith("]")) {
            s = s.substring(1, s.length() - 1).trim();
            if (s.isEmpty()) return new String[0];
            List<String> out = new ArrayList<>();
            for (String part : s.split(",")) {
                String v = part.trim();
                if (v.startsWith("\"") && v.endsWith("\"") && v.length() >= 2) {
                    v = v.substring(1, v.length() - 1).replace("\\\"", "\"");
                }
                out.add(v);
            }
            return out.toArray(new String[0]);
        }
        return new String[0];
    }
}
