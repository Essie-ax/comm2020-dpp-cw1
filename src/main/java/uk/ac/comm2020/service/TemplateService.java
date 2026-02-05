package uk.ac.comm2020.service;

import uk.ac.comm2020.dao.TemplateDao;
import uk.ac.comm2020.model.Template;

import java.sql.SQLException;
import java.util.List;

// Service for template.
// Call DAO, and turn DB error into ServiceException.
public class TemplateService {
    private final TemplateDao templateDao;

    // keep dao here
    public TemplateService(TemplateDao templateDao) {
        this.templateDao = templateDao;
    }

    // Get templates list by category.
    public List<Template> getTemplatesByCategory(String category) {
        try {
            return templateDao.findByCategory(category);
        } catch (SQLException e) {
            // DB fail return 500
            throw new ServiceException("DATABASE_ERROR", "Failed to load templates", 500, null, e);
        }
    }

    // Get one template by id.
    public Template getTemplateById(long templateId) {
        try {
            return templateDao.findById(templateId)
                    .orElseThrow(() -> new ServiceException("NOT_FOUND", "Template not found", 404, null, null));
        } catch (SQLException e) {
            throw new ServiceException("DATABASE_ERROR", "Failed to load template", 500, null, e);
        }
    }
}
