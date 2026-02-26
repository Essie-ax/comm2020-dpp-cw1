package uk.ac.comm2020.util;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpUtil {

    public static String readBody(HttpExchange ex) throws IOException {
        InputStream is = ex.getRequestBody();
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }

    public static void sendJson(HttpExchange ex, int status, ApiResponse response) throws IOException {
        byte[] bytes = response.toJson().getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(status, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.close();
    }

    public static String extractBearerToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring("Bearer ".length()).trim();
    }

    public static Map<String, String> parseVerySimpleJson(String body) {
        Map<String, String> map = new HashMap<>();
        if (body == null || body.isBlank()) return map;
        String s = body.trim();
        if (!s.startsWith("{") || !s.endsWith("}")) return map;
        s = s.substring(1, s.length() - 1).trim();
        if (s.isEmpty()) return map;

        // Split by commas that are NOT inside double-quotes.
        List<String> parts = splitRespectingQuotes(s);
        for (String part : parts) {
            int colon = findColonOutsideQuotes(part);
            if (colon < 0) continue;
            String key = stripQuotes(part.substring(0, colon).trim());
            String val = stripQuotes(part.substring(colon + 1).trim());
            map.put(key, val);
        }
        return map;
    }

    private static List<String> splitRespectingQuotes(String s) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        int start = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"') inQuotes = !inQuotes;
            if (c == ',' && !inQuotes) {
                result.add(s.substring(start, i));
                start = i + 1;
            }
        }
        if (start < s.length()) result.add(s.substring(start));
        return result;
    }

    private static int findColonOutsideQuotes(String s) {
        boolean inQuotes = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"') inQuotes = !inQuotes;
            if (c == ':' && !inQuotes) return i;
        }
        return -1;
    }

    private static String stripQuotes(String s) {
        if (s.startsWith("\"") && s.endsWith("\"")) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }
}