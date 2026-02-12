package uk.ac.comm2020.service;

import com.google.gson.JsonObject;
import uk.ac.comm2020.model.Passport;

import java.util.ArrayList;
import java.util.List;

public class ValidationService {

    // Run validation on one passport
    public List<String> validate(Passport passport) {

        List<String> errors = new ArrayList<>();

        JsonObject fields = passport.getFields();

        // Rule 1: brand is required
        if (!fields.has("brand") || fields.get("brand").getAsString().isBlank()) {
            errors.add("brand is required");
        }

        // Rule 2: category is required
        if (!fields.has("category")) {
            errors.add("category is required");
        }

        // Rule 3: recyclable needs end_of_life
        if (fields.has("recyclable") &&
            fields.get("recyclable").getAsBoolean()) {

            if (!fields.has("end_of_life")) {
                errors.add("recyclable products must provide end_of_life instructions");
            }
        }

        return errors;
    }
}
