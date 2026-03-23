package uk.ac.comm2020.dao;

import uk.ac.comm2020.model.Passport;

import java.sql.SQLException;
import java.util.Optional;

public interface PassportRepository {
    Optional<Passport> findById(long passportId) throws SQLException;
    Passport createDraft(long productId, long templateId, long createdBy) throws SQLException;
    void updateFields(long passportId, String fieldsJson, double completenessScore, double confidenceScore) throws SQLException;
}
