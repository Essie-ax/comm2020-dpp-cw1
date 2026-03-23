package uk.ac.comm2020.service;

import com.google.gson.JsonObject;
import uk.ac.comm2020.model.Evidence;
import uk.ac.comm2020.model.Passport;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class ValidationService {

    // Keep old callers working while new validation can use evidence.
    public List<String> validate(Passport passport) {
        return validate(passport, List.of());
    }

    public List<String> validate(Passport passport, List<Evidence> evidenceList) {
        List<String> errors = new ArrayList<>();
        JsonObject fields = passport.getFields();

        if (isBlankOrMissing(fields, "brand")) {
            errors.add("brand is required");
        }

        if (!fields.has("category")) {
            errors.add("category is required");
        }

        if (fields.has("recyclable") && getBool(fields, "recyclable")) {
            if (isBlankOrMissing(fields, "end_of_life")) {
                errors.add("recyclable products must provide end_of_life instructions");
            }
        }

        if (isBlankOrMissing(fields, "name")) {
            errors.add("name is required");
        }

        if (isBlankOrMissing(fields, "origin")) {
            errors.add("origin is required");
        }

        if (fields.has("weight")) {
            double w = getDouble(fields, "weight");
            if (w <= 0) {
                errors.add("weight must be a positive number");
            }
        }

        if (fields.has("recyclable_percentage")) {
            double pct = getDouble(fields, "recyclable_percentage");
            if (pct < 0 || pct > 100) {
                errors.add("recyclable_percentage must be between 0 and 100");
            }
        }

        validateDates(fields, errors);

        // Organic claim is risky without third-party proof, so we require CERTIFICATE evidence.
        if (fields.has("organic") && getBool(fields, "organic")) {
            boolean hasCert = false;
            for (Evidence e : evidenceList) {
                if ("CERTIFICATE".equalsIgnoreCase(e.getType())) {
                    hasCert = true;
                    break;
                }
            }
            if (!hasCert) {
                errors.add("organic claim requires at least one CERTIFICATE evidence");
            }
        }

        validateCategoryFields(fields, errors);

        return errors;
    }

    private void validateDates(JsonObject fields, List<String> errors) {
        LocalDate mfgDate = parseDate(fields, "manufacture_date");
        LocalDate expDate = parseDate(fields, "expiry_date");

        // Future manufacture dates are usually data entry mistakes.
        if (mfgDate != null && mfgDate.isAfter(LocalDate.now())) {
            errors.add("manufacture_date cannot be a future date");
        }

        if (mfgDate != null && expDate != null && !expDate.isAfter(mfgDate)) {
            errors.add("expiry_date must be after manufacture_date");
        }
    }

    private void validateCategoryFields(JsonObject fields, List<String> errors) {
        if (!fields.has("category")) return;
        String cat = fields.get("category").getAsString();

        if ("Battery".equalsIgnoreCase(cat) && isBlankOrMissing(fields, "chemistry")) {
            errors.add("Battery products must specify chemistry type");
        }

        if ("Electronics".equalsIgnoreCase(cat) && isBlankOrMissing(fields, "compliance_standard")) {
            errors.add("Electronics products must specify compliance_standard");
        }
    }

    private boolean isBlankOrMissing(JsonObject obj, String key) {
        if (!obj.has(key) || obj.get(key).isJsonNull()) return true;
        try {
            return obj.get(key).getAsString().isBlank();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean getBool(JsonObject obj, String key) {
        try {
            return obj.get(key).getAsBoolean();
        } catch (Exception e) {
            return false;
        }
    }

    private double getDouble(JsonObject obj, String key) {
        try {
            return obj.get(key).getAsDouble();
        } catch (Exception e) {
            return 0;
        }
    }

    private LocalDate parseDate(JsonObject obj, String key) {
        if (!obj.has(key) || obj.get(key).isJsonNull()) return null;
        try {
            return LocalDate.parse(obj.get(key).getAsString());
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
