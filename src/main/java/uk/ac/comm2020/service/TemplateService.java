package uk.ac.comm2020.service;

import uk.ac.comm2020.dao.TemplateDao;
import uk.ac.comm2020.model.Role;
import uk.ac.comm2020.model.Template;
import uk.ac.comm2020.service.SessionService.Session;
import uk.ac.comm2020.util.ApiResponse;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class TemplateService {

    private final TemplateDao templateDao;
    private final SessionService sessionService;

    public TemplateService(TemplateDao templateDao) {
        this.templateDao = templateDao;
        this.sessionService = null;
    }

    public TemplateService(TemplateDao templateDao, SessionService sessionService) {
        this.templateDao = templateDao;
        this.sessionService = sessionService;
    }

    public ApiResponse getTemplates(String token) {
        if (sessionService == null) {
            return ApiResponse.error("SERVER_ERROR", "SessionService not configured");
        }

        Session session = sessionService.validateToken(token);
        if (session == null) {
            return ApiResponse.error("UNAUTHORIZED", "Missing or invalid token");
        }

        if (session.role != Role.GAME_KEEPER) {
            return ApiResponse.error("FORBIDDEN", "GameKeeper role required");
        }

        try {
            Map<String, Object> templates = templateDao.getTemplates();
            return ApiResponse.ok(Map.of("templates", templates));
        } catch (SQLException e) {
            return ApiResponse.error("DATABASE_ERROR", "Failed to load templates");
        }
    }

    public List<Template> getTemplatesByCategory(String category) {
        try {
            return templateDao.findByCategory(category);
        } catch (SQLException e) {
            throw new ServiceException("DATABASE_ERROR", "Failed to load templates", 500, null, e);
        }
    }

    public Template getTemplateById(long templateId) {
        try {
            return templateDao.findById(templateId)
                    .orElseThrow(() -> new ServiceException("NOT_FOUND", "Template not found", 404, null, null));
        } catch (SQLException e) {
            throw new ServiceException("DATABASE_ERROR", "Failed to load template", 500, null, e);
        }
    }
}
