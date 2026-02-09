package uk.ac.comm2020.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import uk.ac.comm2020.model.Template;
import uk.ac.comm2020.service.ServiceException;
import uk.ac.comm2020.service.TemplateService;
import uk.ac.comm2020.util.ResponseUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

// Handler for GET /api/templates/{id}.
// Get one template by id, then return json.
public class TemplateByIdHandler implements HttpHandler {
    private final TemplateService templateService;

    // take service from outside, so easy to use and test
    public TemplateByIdHandler(TemplateService templateService) {
        this.templateService = templateService;
    }

    // Only handle GET. Other method not needed here.
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            // only GET is ok
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                ResponseUtil.sendJson(exchange, 405,
                        ResponseUtil.error("METHOD_NOT_ALLOWED", "Only GET is allowed", null));
                return;
            }

            // get id from path
            String path = exchange.getRequestURI().getPath();
            String idPart = path.substring("/api/templates/".length());

            long templateId;
            try {
                templateId = Long.parseLong(idPart);
            } catch (NumberFormatException e) {
                // id not a number return 400
                ResponseUtil.sendJson(exchange, 400,
                        ResponseUtil.error("BAD_REQUEST", "templateId must be a number", null));
                return;
            }

            // ask service for template
            Template template = templateService.getTemplateById(templateId);

            // make response data
            Map<String, Object> data = new HashMap<>();
            data.put("templateId", template.getTemplateId());
            data.put("category", template.getCategory());
            data.put("requiredFields", template.getRequiredFields());
            data.put("optionalFields", template.getOptionalFields());
            data.put("ruleSetId", template.getRuleSetId());

            ResponseUtil.sendJson(exchange, 200, ResponseUtil.success(data));
        } catch (ServiceException e) {
            // service error, keep status/code/message
            ResponseUtil.sendJson(exchange, e.getStatus(),
                    ResponseUtil.error(e.getCode(), e.getMessage(), e.getDetails()));
        } catch (Exception e) {
            // other error return 500
            ResponseUtil.sendJson(exchange, 500,
                    ResponseUtil.error("INTERNAL_ERROR", "Unexpected server error", null));
        }
    }
}
