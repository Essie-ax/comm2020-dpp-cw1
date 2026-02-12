package uk.ac.comm2020.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import uk.ac.comm2020.model.Passport;

import java.util.List;

public class ScoringService {

    // Calculate completeness score based on REQUIRED fields from template
    public double calculateCompleteness(Passport passport, List<String> requiredFields) {

        JsonObject fields = passport.getFields();

        if (requiredFields == null || requiredFields.isEmpty()) {
            return 100.0;
        }

        int filled = 0;

        for (String field : requiredFields) {
            if (!fields.has(field)) continue;

            JsonElement el = fields.get(field);
            if (el == null || el.isJsonNull()) continue;

            // if it's a primitive string/number/bool, count when not blank
            String v = el.getAsString().trim();
            if (!v.isEmpty()) filled++;
        }

        return (double) filled / requiredFields.size() * 100.0;
    }

    // Very simple confidence score
    public double calculateConfidence(int evidenceCount) {
        return Math.min(evidenceCount * 20.0, 100.0);
    }
}
