package uk.ac.comm2020.model;

import com.google.gson.JsonObject;

// Passport model.
public class Passport {
    private final long passportId;
    private final long productId;
    private final long templateId;
    private final String status;
    private final JsonObject fields;
    private final double completenessScore;
    private final double confidenceScore;

    // Build one passport object.
    public Passport(long passportId,
                    long productId,
                    long templateId,
                    String status,
                    JsonObject fields,
                    double completenessScore,
                    double confidenceScore) {
        this.passportId = passportId;
        this.productId = productId;
        this.templateId = templateId;
        this.status = status;
        this.fields = fields;
        this.completenessScore = completenessScore;
        this.confidenceScore = confidenceScore;
    }

    // id of passport row
    public long getPassportId() {
        return passportId;
    }

    // which product it links to
    public long getProductId() {
        return productId;
    }

    // which template it uses
    public long getTemplateId() {
        return templateId;
    }

    // status like DRAFT / DONE (depends on your code)
    public String getStatus() {
        return status;
    }

    // all fields data in json
    public JsonObject getFields() {
        return fields;
    }

    // score for how full it is
    public double getCompletenessScore() {
        return completenessScore;
    }

    // score for how sure it is
    public double getConfidenceScore() {
        return confidenceScore;
    }
}
