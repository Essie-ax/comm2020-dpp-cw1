package uk.ac.comm2020.service;

import uk.ac.comm2020.dao.EvidenceRepository;
import uk.ac.comm2020.model.Evidence;

import java.sql.SQLException;
import java.util.List;

public class EvidenceService {

    private final EvidenceRepository evidenceRepo;

    public EvidenceService(EvidenceRepository evidenceRepo) {
        this.evidenceRepo = evidenceRepo;
    }

    public void addEvidence(Evidence evidence) throws SQLException {
        evidenceRepo.save(evidence);
    }

    public List<Evidence> getEvidenceForPassport(long passportId) throws SQLException {
        return evidenceRepo.findByPassportId(passportId);
    }
}
