package uk.ac.comm2020.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import uk.ac.comm2020.dao.PassportDao;
import uk.ac.comm2020.dao.ProductDao;
import uk.ac.comm2020.dao.TemplateDao;
import uk.ac.comm2020.model.Passport;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Service for passport.
// Do checks + simple rules here, then call DAO.
public class PassportService {
    private final PassportDao passportDao;
    private final TemplateDao templateDao;
    private final ProductDao productDao;

    // keep all dao here
    public PassportService(PassportDao passportDao, TemplateDao templateDao, ProductDao productDao) {
        this.passportDao = passportDao;
        this.templateDao = templateDao;
        this.productDao = productDao;
    }

    // Make a draft passport.
    // Check product and template first, so DB insert not waste.
    public Passport createDraft(long productId, long templateId, long createdBy) {
        try {
            // product must exist
            if (!productDao.exists(productId)) {
                throw new ServiceException("NOT_FOUND", "Product not found", 404, null, null);
            }

            // template must exist
            if (!templateDao.exists(templateId)) {
                throw new ServiceException("NOT_FOUND", "Template not found", 404, null, null);
            }

            return passportDao.createDraft(productId, templateId, createdBy);
        } catch (SQLException e) {
            // DB problem, wrap it as service error
            throw new ServiceException("DATABASE_ERROR", "Failed to create passport", 500, null, e);
        }
    }

    // Update fields for one passport.
    // Also check required fields, and update score.
    public Passport updateFields(long passportId, JsonObject fields) {
        try {
            // passport must exist
            Passport passport = passportDao.findById(passportId)
                    .orElseThrow(() -> new ServiceException("NOT_FOUND", "Passport not found", 404, null, null));

            // need required fields list from template
            List<String> requiredFields = templateDao.findRequiredFieldsById(passport.getTemplateId())
                    .orElseThrow(() -> new ServiceException("NOT_FOUND", "Template not found", 404, null, null));

            // check required fields are filled
            List<String> missing = validateRequiredFields(requiredFields, fields);
            if (!missing.isEmpty()) {
                // tell client which keys are missing
                ServiceException error = new ServiceException(
                        "VALIDATION_ERROR",
                        "Missing required fields",
                        400,
                        ServiceException.details("missingFields", missing),
                        null
                );
                throw error;
            }

            // calc score, then save
            double completeness = calculateCompleteness(requiredFields, fields);

            // confidence not ready now, so keep 0
            double confidence = 0.0;

            passportDao.updateFields(passportId, fields.toString(), completeness, confidence);

            // return new object with updated data
            return new Passport(
                    passport.getPassportId(),
                    passport.getProductId(),
                    passport.getTemplateId(),
                    passport.getStatus(),
                    fields,
                    completeness,
                    confidence
            );
        } catch (SQLException e) {
            throw new ServiceException("DATABASE_ERROR", "Failed to update passport", 500, null, e);
        }
    }

    // Get one passport by id.
    public Passport getPassport(long passportId) {
        try {
            return passportDao.findById(passportId)
                    .orElseThrow(() -> new ServiceException("NOT_FOUND", "Passport not found", 404, null, null));
        } catch (SQLException e) {
            throw new ServiceException("DATABASE_ERROR", "Failed to load passport", 500, null, e);
        }
    }

    // Check all required keys, return missing list.
    private List<String> validateRequiredFields(List<String> requiredFields, JsonObject fields) {
        List<String> missing = new ArrayList<>();
        for (String key : requiredFields) {
            if (!isFilled(fields, key)) {
                missing.add(key);
            }
        }
        return missing;
    }

    // Tell if one key is filled or not.
    // Empty string / empty list / empty object -> treat as not filled.
    private boolean isFilled(JsonObject fields, String key) {
        if (fields == null || !fields.has(key)) {
            return false;
        }

        JsonElement value = fields.get(key);
        if (value == null || value.isJsonNull()) {
            return false;
        }

        if (value.isJsonPrimitive()) {
            if (value.getAsJsonPrimitive().isString()) {
                return !value.getAsString().isBlank();
            }
            return true;
        }

        if (value.isJsonArray()) {
            return value.getAsJsonArray().size() > 0;
        }

        if (value.isJsonObject()) {
            return value.getAsJsonObject().size() > 0;
        }

        return true;
    }

    // Simple completeness score: filled / total required.
    private double calculateCompleteness(List<String> requiredFields, JsonObject fields) {
        if (requiredFields.isEmpty()) {
            return 1.0;
        }

        int filled = 0;
        for (String key : requiredFields) {
            if (isFilled(fields, key)) {
                filled += 1;
            }
        }

        return (double) filled / (double) requiredFields.size();
    }
}
