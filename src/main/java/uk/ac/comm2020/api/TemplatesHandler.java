package uk.ac.comm2020.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import uk.ac.comm2020.model.Template;
import uk.ac.comm2020.service.ServiceException;
import uk.ac.comm2020.service.TemplateService;
import uk.ac.comm2020.util.RequestUtil;
import uk.ac.comm2020.util.ResponseUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Handler for templates list api.
// Use category in query, then return templates list.
public class TemplatesHandler implements HttpHandler {
    private final TemplateService templateService;

    // take service from outside, easy to test
    public TemplatesHandler(TemplateService templateService) {
        this.templateService = templateService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                ResponseUtil.sendJson(exchange, 405,
                        ResponseUtil.error("METHOD_NOT_ALLOWED", "Only GET is allowed", null));
                return;
            }

            // read query from url
            Map<String, String> query = RequestUtil.parseQuery(exchange.getRequestURI());

            // category is must, no category then no data
            String category = query.get("category");
            if (category == null || category.isBlank()) {
                ResponseUtil.sendJson(exchange, 400,
                        ResponseUtil.error("BAD_REQUEST", "Query param 'category' is required", null));
                return;
            }

            // get list from service
            List<Template> templates = templateService.getTemplatesByCategory(category);

            // wrap and send back
            Map<String, Object> data = new HashMap<>();
            data.put("templates", templates);
            ResponseUtil.sendJson(exchange, 200, ResponseUtil.success(data));
        } catch (ServiceException e) {
            // service error, keep status and msg
            ResponseUtil.sendJson(exchange, e.getStatus(),
                    ResponseUtil.error(e.getCode(), e.getMessage(), e.getDetails()));
        } catch (Exception e) {
            // other error return 500
            ResponseUtil.sendJson(exchange, 500,
                    ResponseUtil.error("INTERNAL_ERROR", "Unexpected server error", null));
        }
    }
}
