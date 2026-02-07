package uk.ac.comm2020.service;

import uk.ac.comm2020.dao.TemplateDao;
import uk.ac.comm2020.model.Role;
import uk.ac.comm2020.service.SessionService.Session;
import uk.ac.comm2020.util.ApiResponse;

import java.util.Map;

public class TemplateService {

    private final TemplateDao templateDao;
    private final SessionService sessionService;

    public TemplateService(TemplateDao templateDao, SessionService sessionService) {
        this.templateDao = templateDao;
        this.sessionService = sessionService;
    }

    public ApiResponse getTemplates(String token) {
        Session session = sessionService.validateToken(token);
        if (session == null) {
            return ApiResponse.error("UNAUTHORIZED", "Missing or invalid token");
        }

        if (session.role != Role.GAME_KEEPER) {
            return ApiResponse.error("FORBIDDEN", "GameKeeper role required");
        }

        Map<String, Object> templates = templateDao.getTemplates();
        return ApiResponse.ok(Map.of("templates", templates));
    }
}