package uk.ac.comm2020.util;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

// Request helper.
// Read body, and parse query params.
public final class RequestUtil {
    // no new RequestUtil()
    private RequestUtil() {
    }

    // Read request body as utf-8 string.
    public static String readBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    // Parse query part in URL, like ?a=1&b=2
    public static Map<String, String> parseQuery(URI uri) {
        String query = uri.getRawQuery();
        Map<String, String> params = new HashMap<>();

        // no query -> empty map
        if (query == null || query.isBlank()) {
            return params;
        }

        // split by &
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            if (idx <= 0) {
                continue;
            }

            // decode key/value
            String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
            String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);

            params.put(key, value);
        }

        return params;
    }
}
