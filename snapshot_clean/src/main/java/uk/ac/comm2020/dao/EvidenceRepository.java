package uk.ac.comm2020.dao;

import uk.ac.comm2020.model.Evidence;

import java.sql.SQLException;
import java.util.List;

public interface EvidenceRepository {
    void save(Evidence evidence) throws SQLException;
    List<Evidence> findByPassportId(long passportId) throws SQLException;
}
