package uk.ac.comm2020.service;

import uk.ac.comm2020.dao.PassportDao;
import uk.ac.comm2020.model.Evidence;
import uk.ac.comm2020.model.Passport;

import java.sql.SQLException;
import java.util.List;

public class PassportValidationService {

    private final PassportDao passportDao;
    private final EvidenceService evidenceService;
    private final ValidationService validationService;
    private final ScoringService scoringService;

    public PassportValidationService(PassportDao passportDao,
                                     EvidenceService evidenceService,
                                     ValidationService validationService,
                                     ScoringService scoringService) {
        this.passportDao = passportDao;
        this.evidenceService = evidenceService;
        this.validationService = validationService;
        this.scoringService = scoringService;
    }

    public ValidationResult validatePassport(Passport passport) throws SQLException {

        // 1. run validation rules
        List<String> errors = validationService.validate(passport);

        // 2. get evidence
        List<Evidence> evidenceList =
                evidenceService.getEvidenceForPassport(passport.getPassportId());

        // 3. calculate scores
        double completeness =
                scoringService.calculateCompleteness(passport);

        double confidence =
                scoringService.calculateConfidence(evidenceList.size());

        return new ValidationResult(errors, completeness, confidence);
    }
}
