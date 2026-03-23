package uk.ac.comm2020.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import uk.ac.comm2020.model.Passport;
import uk.ac.comm2020.service.PassportService;
import uk.ac.comm2020.service.ServiceException;
import uk.ac.comm2020.util.JsonUtil;
import uk.ac.comm2020.util.RequestUtil;
import uk.ac.comm2020.util.ResponseUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

// Do routing + basic check here. Real logic stay in service.
public class PassportsHandler implements HttpHandler {
    // base path for this api
    private static final String BASE_PATH = "/api/passports";

    private final PassportService passportService;

    // take service from outside, easy for test and reuse
    public PassportsHandler(PassportService passportService) {
        this.passportService = passportService;
    }

    // Main entry. Look at path, then dispatch.
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        try {
            if (path.equals(BASE_PATH) || path.equals(BASE_PATH + "/")) {
                handleCollection(exchange);
                return;
            }

            if (path.startsWith(BASE_PATH + "/")) {
                handleItem(exchange, path.substring(BASE_PATH.length() + 1));
                return;
            }

            // path not match, just 404
            ResponseUtil.sendJson(exchange, 404,
                    ResponseUtil.error("NOT_FOUND", "Endpoint not found", null));
        } catch (ServiceException e) {
            // service already know how to explain the error
            ResponseUtil.sendJson(exchange, e.getStatus(),
                    ResponseUtil.error(e.getCode(), e.getMessage(), e.getDetails()));
        } catch (Exception e) {
            // anything else, treat as server problem
            ResponseUtil.sendJson(exchange, 500,
                    ResponseUtil.error("INTERNAL_ERROR", "Unexpected server error", null));
        }
    }

    // create draft with productId + templateId
    private void handleCollection(HttpExchange exchange) throws IOException {
        // Only POST here, other method no meaning
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            ResponseUtil.sendJson(exchange, 405,
                    ResponseUtil.error("METHOD_NOT_ALLOWED", "Only POST is allowed", null));
            return;
        }

        // read json body, if broken will throw 400
        JsonObject json = parseJsonBody(exchange);

        // check required params
        JsonElement productElement = json.get("productId");
        JsonElement templateElement = json.get("templateId");
        if (productElement == null || templateElement == null) {
            ResponseUtil.sendJson(exchange, 400,
                    ResponseUtil.error("BAD_REQUEST", "productId and templateId are required", null));
            return;
        }

        // parse as number, if not number then reject
        Long productId = parseLong(productElement);
        Long templateId = parseLong(templateElement);
        if (productId == null || templateId == null) {
            ResponseUtil.sendJson(exchange, 400,
                    ResponseUtil.error("BAD_REQUEST", "productId and templateId must be numbers", null));
            return;
        }

        // get user id from header, missing then use default
        long createdBy = resolveUserId(exchange);

        // create draft passport
        Passport passport = passportService.createDraft(productId, templateId, createdBy);

        // return small response, just id + status
        Map<String, Object> data = new HashMap<>();
        data.put("passportId", passport.getPassportId());
        data.put("status", passport.getStatus());
        ResponseUtil.sendJson(exchange, 200, ResponseUtil.success(data));
    }

    // project endpoint
    private void handleItem(HttpExchange exchange, String idPart) throws IOException {
        long passportId;
        try {
            passportId = Long.parseLong(idPart);
        } catch (NumberFormatException e) {
            // id is not number, no need continue
            ResponseUtil.sendJson(exchange, 400,
                    ResponseUtil.error("BAD_REQUEST", "passportId must be a number", null));
            return;
        }

        // method routing
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            handleGet(exchange, passportId);
            return;
        }
        if ("PUT".equalsIgnoreCase(exchange.getRequestMethod())) {
            handlePut(exchange, passportId);
            return;
        }

        ResponseUtil.sendJson(exchange, 405,
                ResponseUtil.error("METHOD_NOT_ALLOWED", "Only GET or PUT is allowed", null));
    }

    private void handlePut(HttpExchange exchange, long passportId) throws IOException {
        JsonObject json = parseJsonBody(exchange);

        // fields must be object, otherwise service cannot update
        JsonElement fieldsElement = json.get("fields");
        if (fieldsElement == null || !fieldsElement.isJsonObject()) {
            ResponseUtil.sendJson(exchange, 400,
                    ResponseUtil.error("BAD_REQUEST", "fields object is required", null));
            return;
        }

        JsonObject fields = fieldsElement.getAsJsonObject();

        // update only fields, other things not touch here
        Passport updated = passportService.updateFields(passportId, fields);

        // keep response short
        Map<String, Object> data = new HashMap<>();
        data.put("passportId", updated.getPassportId());
        data.put("status", updated.getStatus());
        ResponseUtil.sendJson(exchange, 200, ResponseUtil.success(data));
    }

    // return all info, include scores and fields
    private void handleGet(HttpExchange exchange, long passportId) throws IOException {
        Passport passport = passportService.getPassport(passportId);

        // map to response json
        Map<String, Object> data = new HashMap<>();
        data.put("passportId", passport.getPassportId());
        data.put("productId", passport.getProductId());
        data.put("templateId", passport.getTemplateId());
        data.put("status", passport.getStatus());
        data.put("fields", passport.getFields());
        data.put("completenessScore", passport.getCompletenessScore());
        data.put("confidenceScore", passport.getConfidenceScore());
        ResponseUtil.sendJson(exchange, 200, ResponseUtil.success(data));
    }

    // Read X-User-Id from header.
    // If missing or invalid, fallback to 1.
    private long resolveUserId(HttpExchange exchange) {
        String header = exchange.getRequestHeaders().getFirst("X-User-Id");
        if (header == null || header.isBlank()) {
            return 1L;
        }
        try {
            long id = Long.parseLong(header.trim());
            return id > 0 ? id : 1L;
        } catch (NumberFormatException e) {
            return 1L;
        }
    }

    // Safe parse long from json element.
    // If not a number, just return null.
    private Long parseLong(JsonElement element) {
        if (element == null || !element.isJsonPrimitive()) {
            return null;
        }
        try {
            return element.getAsLong();
        } catch (Exception e) {
            return null;
        }
    }

    // Read request body and parse json.
    // throw ServiceException, then become 400.
    private JsonObject parseJsonBody(HttpExchange exchange) throws IOException {
        String body = RequestUtil.readBody(exchange);
        try {
            return JsonUtil.parseObject(body);
        } catch (Exception e) {
            throw new ServiceException("BAD_REQUEST", "Invalid JSON body", 400, null, e);
        }
    }
}
