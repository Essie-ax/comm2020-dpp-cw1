package uk.ac.comm2020.model;

import java.util.List;

// Template model.
public class Template {
    private final long templateId;
    private final String category;
    private final List<String> requiredFields;
    private final List<String> optionalFields;
    private final int ruleSetId;

    // Build one template object.
    public Template(long templateId, String category, List<String> requiredFields, List<String> optionalFields, int ruleSetId) {
        this.templateId = templateId;
        this.category = category;
        this.requiredFields = requiredFields;
        this.optionalFields = optionalFields;
        this.ruleSetId = ruleSetId;
    }

    // template id in DB
    public long getTemplateId() {
        return templateId;
    }

    // category name for this template
    public String getCategory() {
        return category;
    }

    // fields user must fill
    public List<String> getRequiredFields() {
        return requiredFields;
    }

    // fields user can skip if they want
    public List<String> getOptionalFields() {
        return optionalFields;
    }

    // id for rule set (used by rule engine maybe)
    public int getRuleSetId() {
        return ruleSetId;
    }
}
