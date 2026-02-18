package uk.ac.comm2020.service;

import uk.ac.comm2020.dao.EvidenceDao;
import uk.ac.comm2020.model.Evidence;

import java.sql.SQLException;
import java.util.List;

public class EvidenceService {

    private final EvidenceDao evidenceDao;

    public EvidenceService(EvidenceDao evidenceDao) {
        this.evidenceDao = evidenceDao;
    }

    // Add one evidence
    public void addEvidence(Evidence evidence) throws SQLException {
        evidenceDao.save(evidence);
    }

    // Get all evidence for one passport
    public List<Evidence> getEvidenceForPassport(long passportId) throws SQLException {
        return evidenceDao.findByPassportId(passportId);
    }
}
