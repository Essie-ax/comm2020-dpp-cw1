package uk.ac.comm2020.service;

import java.util.List;

public class ValidationResult {

    private final List<String> errors;
    private final double completenessScore;
    private final double confidenceScore;

    public ValidationResult(List<String> errors,
                            double completenessScore,
                            double confidenceScore) {
        this.errors = errors;
        this.completenessScore = completenessScore;
        this.confidenceScore = confidenceScore;
    }

    public List<String> getErrors() {
        return errors;
    }

    public double getCompletenessScore() {
        return completenessScore;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }
}
