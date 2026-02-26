package uk.ac.comm2020.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import uk.ac.comm2020.dao.PassportRepository;
import uk.ac.comm2020.model.Passport;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ComparisonService {

    private final PassportRepository passportRepo;

    public ComparisonService(PassportRepository passportRepo) {
        this.passportRepo = passportRepo;
    }

    public Map<String, Object> compare(long id1, long id2) throws SQLException {
        Optional<Passport> opt1 = passportRepo.findById(id1);
        Optional<Passport> opt2 = passportRepo.findById(id2);

        if (opt1.isEmpty()) {
            throw new IllegalArgumentException("Passport not found: " + id1);
        }
        if (opt2.isEmpty()) {
            throw new IllegalArgumentException("Passport not found: " + id2);
        }

        Passport p1 = opt1.get();
        Passport p2 = opt2.get();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("passport1", passportToMap(p1));
        result.put("passport2", passportToMap(p2));
        result.put("fieldDiffs", buildFieldDiffs(p1.getFields(), p2.getFields()));
        result.put("scoreDiff", buildScoreDiff(p1, p2));
        return result;
    }

    private Map<String, Object> passportToMap(Passport p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("passportId", p.getPassportId());
        m.put("productId", p.getProductId());
        m.put("templateId", p.getTemplateId());
        m.put("status", p.getStatus());
        m.put("completenessScore", p.getCompletenessScore());
        m.put("confidenceScore", p.getConfidenceScore());
        m.put("fields", fieldsToMap(p.getFields()));
        return m;
    }

    private Map<String, Object> fieldsToMap(JsonObject fields) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : fields.entrySet()) {
            m.put(entry.getKey(), primitiveToJava(entry.getValue()));
        }
        return m;
    }

    // Keep original types so the frontend can display numbers/booleans correctly.
    private Object primitiveToJava(JsonElement el) {
        if (!el.isJsonPrimitive()) return el.toString();
        JsonPrimitive p = el.getAsJsonPrimitive();
        if (p.isBoolean()) return p.getAsBoolean();
        if (p.isNumber()) return p.getAsDouble();
        return p.getAsString();
    }

    private List<Map<String, Object>> buildFieldDiffs(JsonObject f1, JsonObject f2) {
        Set<String> allKeys = new LinkedHashSet<>();
        allKeys.addAll(f1.keySet());
        allKeys.addAll(f2.keySet());

        List<Map<String, Object>> matches = new ArrayList<>();
        List<Map<String, Object>> diffs = new ArrayList<>();

        for (String key : allKeys) {
            String v1 = f1.has(key) ? f1.get(key).getAsString() : null;
            String v2 = f2.has(key) ? f2.get(key).getAsString() : null;
            boolean same = v1 != null && v1.equals(v2);

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("key", key);
            row.put("value1", v1);
            row.put("value2", v2);
            row.put("match", same);

            if (same) {
                matches.add(row);
            } else {
                diffs.add(row);
            }
        }

        // Show mismatches first so differences stand out immediately.
        diffs.addAll(matches);
        return diffs;
    }

    private Map<String, Object> buildScoreDiff(Passport p1, Passport p2) {
        Map<String, Object> scoreDiff = new LinkedHashMap<>();
        scoreDiff.put("completeness", scoreRow(p1.getCompletenessScore(), p2.getCompletenessScore()));
        scoreDiff.put("confidence", scoreRow(p1.getConfidenceScore(), p2.getConfidenceScore()));
        return scoreDiff;
    }

    private Map<String, Object> scoreRow(double v1, double v2) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("value1", v1);
        row.put("value2", v2);
        // Round to 1 decimal to avoid floating-point noise in the diff label.
        row.put("diff", Math.round((v2 - v1) * 10.0) / 10.0);
        return row;
    }
}
