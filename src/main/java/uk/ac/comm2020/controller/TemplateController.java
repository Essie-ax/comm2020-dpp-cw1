package uk.ac.comm2020.controller;

import com.sun.net.httpserver.HttpExchange;
import uk.ac.comm2020.service.TemplateService;
import uk.ac.comm2020.util.ApiResponse;
import uk.ac.comm2020.util.HttpUtil;

import java.io.IOException;

public class TemplateController {

    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    public void handleTemplates(HttpExchange ex) throws IOException {
        if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
            HttpUtil.sendJson(ex, 405, ApiResponse.error("METHOD_NOT_ALLOWED", "Only GET is allowed"));
            return;
        }

        String auth = ex.getRequestHeaders().getFirst("Authorization");
        String token = HttpUtil.extractBearerToken(auth);

        ApiResponse response = templateService.getTemplates(token);
        HttpUtil.sendJson(ex, response.getStatus(), response);
    }
}