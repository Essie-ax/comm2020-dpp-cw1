package uk.ac.comm2020.util;

import java.util.HashMap;
import java.util.Map;

public class ApiResponse {

    private final boolean success;
    private final Map<String, Object> data;
    private final Map<String, Object> error;

    private ApiResponse(boolean success, Map<String, Object> data, Map<String, Object> error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }

    public static ApiResponse ok(Map<String, Object> data) {
        return new ApiResponse(true, data, null);
    }

    public static ApiResponse error(String code, String message) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("code", code);
        errorDetails.put("message", message);
        return new ApiResponse(false, null, errorDetails);
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"success\":").append(success).append(",");
        if (success) {
            sb.append("\"data\":").append(mapToJson(data));
        } else {
            sb.append("\"error\":").append(mapToJson(error));
        }
        sb.append("}");
        return sb.toString();
    }

    private String mapToJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        map.forEach((key, value) -> {
            sb.append("\"").append(JsonUtil.escape(key)).append("\":");
            if (value instanceof String) {
                sb.append("\"").append(JsonUtil.escape(value.toString())).append("\",");
            } else {
                sb.append(value).append(",");
            }
        });
        if (!map.isEmpty()) {
            sb.setLength(sb.length() - 1);
        }
        sb.append("}");
        return sb.toString();
    }

    public int getStatus() {
        if (success) return 200;
        String code = error != null ? String.valueOf(error.get("code")) : "";
        switch (code) {
            case "BAD_REQUEST": return 400;
            case "UNAUTHORIZED": return 401;
            case "FORBIDDEN": return 403;
            case "NOT_FOUND": return 404;
            case "METHOD_NOT_ALLOWED": return 405;
            case "DATABASE_ERROR":
            case "SERVER_ERROR": return 500;
            default: return 400;
        }
    }

    public boolean isSuccess() {
        return success;
    }

    public Map<String, Object> getData() {
        return data;
    }
}