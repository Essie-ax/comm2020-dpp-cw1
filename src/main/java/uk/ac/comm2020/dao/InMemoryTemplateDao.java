package uk.ac.comm2020.dao;

import java.util.HashMap;
import java.util.Map;

public class InMemoryTemplateDao implements TemplateDao {

    @Override
    public Map<String, Object> getTemplates() {
        Map<String, Object> template = new HashMap<>();
        template.put("templateId", 1);
        template.put("category", "Battery");
        template.put("requiredFields", new String[]{"name", "brand", "origin", "chemistry"});
        template.put("optionalFields", new String[]{"recyclability", "warrantyMonths"});
        template.put("ruleSetId", 1);

        return Map.of("templates", new Object[]{template});
    }
}