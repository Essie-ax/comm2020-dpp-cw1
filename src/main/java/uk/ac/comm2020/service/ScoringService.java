package uk.ac.comm2020.service;

import com.google.gson.JsonObject;
import uk.ac.comm2020.model.Passport;

public class ScoringService {

    // Calculate completeness score
    public double calculateCompleteness(Passport passport) {

        JsonObject fields = passport.getFields();

        String[] requiredFields = {
                "brand",
                "category",
                "manufacturer"
        };

        int filled = 0;

        for (String field : requiredFields) {
            if (fields.has(field) &&
                !fields.get(field).getAsString().isBlank()) {

                filled++;
            }
        }

        return (double) filled / requiredFields.length * 100.0;
    }


    // Very simple confidence score
    public double calculateConfidence(int evidenceCount) {

        // cap at 100
        return Math.min(evidenceCount * 20.0, 100.0);
    }
}
