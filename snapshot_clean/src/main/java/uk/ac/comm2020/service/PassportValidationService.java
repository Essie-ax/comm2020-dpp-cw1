package uk.ac.comm2020.service;

import uk.ac.comm2020.dao.PassportRepository;
import uk.ac.comm2020.model.Evidence;
import uk.ac.comm2020.model.Passport;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class PassportValidationService {

    private final PassportRepository passportRepo;
    private final EvidenceService evidenceService;
    private final ValidationService validationService;
    private final ScoringService scoringService;

    public PassportValidationService(PassportRepository passportRepo,
                                     EvidenceService evidenceService,
                                     ValidationService validationService,
                                     ScoringService scoringService) {
        this.passportRepo = passportRepo;
        this.evidenceService = evidenceService;
        this.validationService = validationService;
        this.scoringService = scoringService;
    }

    public ValidationResult validatePassport(long passportId) throws SQLException {
        Optional<Passport> opt = passportRepo.findById(passportId);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Passport not found: " + passportId);
        }
        Passport p = opt.get();
        return validatePassport(p);
    }

    public ValidationResult validatePassport(Passport passport) throws SQLException {
        List<Evidence> evidenceList =
                evidenceService.getEvidenceForPassport(passport.getPassportId());

        // pass evidence to validator so it can check evidence-dependent rules too
        List<String> errors = validationService.validate(passport, evidenceList);

        double completeness = scoringService.calculateCompleteness(
                passport, List.of("name", "brand", "category", "origin", "weight"));
        double confidence = scoringService.calculateConfidence(evidenceList.size());

        return new ValidationResult(errors, completeness, confidence);
    }
}
