package uk.ac.comm2020.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

// Simple static file handler.
// Read file from classpath, then send it to browser.
public class StaticFileHandler implements HttpHandler {
    private final String basePath;
    private final Map<String, String> contentTypes;

    // basePath is folder in resources (like "static")
    public StaticFileHandler(String basePath) {
        // make sure no leading "/", so classloader can find it
        this.basePath = basePath.startsWith("/") ? basePath.substring(1) : basePath;
        this.contentTypes = defaultContentTypes();
    }

    // Only handle GET, other method just block.
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        // get path from url
        URI uri = exchange.getRequestURI();
        String path = uri.getPath();

        // if user visit "/", send them to home page
        if (path == null || path.isBlank() || "/".equals(path)) {
            redirect(exchange, "/authoring.html");
            return;
        }

        // build classpath resource path
        String resourcePath = basePath + (path.startsWith("/") ? path : "/" + path);

        // read file as stream, then send bytes out
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (stream == null) {
                // file not found
                exchange.sendResponseHeaders(404, -1);
                return;
            }

            byte[] bytes = stream.readAllBytes();

            // set content type by file ext
            exchange.getResponseHeaders().set("Content-Type", contentTypeFor(path));

            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
        } finally {
            // always close exchange
            exchange.close();
        }
    }

    // send 302 redirect
    private void redirect(HttpExchange exchange, String location) throws IOException {
        exchange.getResponseHeaders().set("Location", location);
        exchange.sendResponseHeaders(302, -1);
        exchange.close();
    }

    // pick content type by suffix, if not match then use default
    private String contentTypeFor(String path) {
        String lower = path.toLowerCase();
        for (Map.Entry<String, String> entry : contentTypes.entrySet()) {
            if (lower.endsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return "application/octet-stream";
    }

    // basic content types for common files
    private Map<String, String> defaultContentTypes() {
        Map<String, String> map = new HashMap<>();
        map.put(".html", "text/html; charset=" + StandardCharsets.UTF_8);
        map.put(".css", "text/css; charset=" + StandardCharsets.UTF_8);
        map.put(".js", "application/javascript; charset=" + StandardCharsets.UTF_8);
        map.put(".json", "application/json; charset=" + StandardCharsets.UTF_8);
        map.put(".png", "image/png");
        map.put(".jpg", "image/jpeg");
        map.put(".jpeg", "image/jpeg");
        map.put(".svg", "image/svg+xml");
        return map;
    }
}
