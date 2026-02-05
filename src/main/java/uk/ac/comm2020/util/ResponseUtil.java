package uk.ac.comm2020.util;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

// Response helper.
// Build success/error body, and send json out.
public final class ResponseUtil {
    
    private ResponseUtil() {
    }

    // success wrapper: { success: true, data: ... }
    public static Map<String, Object> success(Object data) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("data", data);
        return body;
    }

    // error wrapper: { success: false, error: { code, message, details? } }
    public static Map<String, Object> error(String code, String message, Map<String, Object> details) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);

        Map<String, Object> error = new HashMap<>();
        error.put("code", code);
        error.put("message", message);

        // details is optional
        if (details != null) {
            error.put("details", details);
        }

        body.put("error", error);
        return body;
    }

    // Send json to client.
    // Set content-type, write bytes, then close output.
    public static void sendJson(HttpExchange exchange, int status, Object body) throws IOException {
        byte[] bytes = JsonUtil.toJson(body).getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);

        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }
}
