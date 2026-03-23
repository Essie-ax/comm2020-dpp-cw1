package uk.ac.comm2020.model;

public class Evidence {

    private final long evidenceId;
    private final long passportId;
    private final String fieldKey;   // which claim it supports
    private final String type;       // CERTIFICATE / AUDIT / TEST_REPORT
    private final String issuer;
    private final String summary;

    public Evidence(long evidenceId,
                    long passportId,
                    String fieldKey,
                    String type,
                    String issuer,
                    String summary) {

        this.evidenceId = evidenceId;
        this.passportId = passportId;
        this.fieldKey = fieldKey;
        this.type = type;
        this.issuer = issuer;
        this.summary = summary;
    }

    public long getEvidenceId() {
        return evidenceId;
    }

    public long getPassportId() {
        return passportId;
    }

    public String getFieldKey() {
        return fieldKey;
    }

    public String getType() {
        return type;
    }

    public String getIssuer() {
        return issuer;
    }

    public String getSummary() {
        return summary;
    }
}
