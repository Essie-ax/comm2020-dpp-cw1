package uk.ac.comm2020.util;

import java.util.HashMap;
import java.util.List;
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
            sb.append("\"data\":").append(valueToJson(data));
        } else {
            sb.append("\"error\":").append(valueToJson(error));
        }
        sb.append("}");
        return sb.toString();
    }

    /** Convert any value to JSON string. Handles Map, List, array, String, Number, Boolean, null. */
    @SuppressWarnings("unchecked")
    private static String valueToJson(Object value) {
        if (value == null) return "null";

        if (value instanceof String) {
            return "\"" + JsonUtil.escape((String) value) + "\"";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) value;
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (!first) sb.append(",");
                sb.append("\"").append(JsonUtil.escape(entry.getKey())).append("\":");
                sb.append(valueToJson(entry.getValue()));
                first = false;
            }
            sb.append("}");
            return sb.toString();
        }
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(valueToJson(list.get(i)));
            }
            sb.append("]");
            return sb.toString();
        }
        if (value instanceof Object[]) {
            Object[] arr = (Object[]) value;
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < arr.length; i++) {
                if (i > 0) sb.append(",");
                sb.append(valueToJson(arr[i]));
            }
            sb.append("]");
            return sb.toString();
        }
        // Fallback: treat as string
        return "\"" + JsonUtil.escape(value.toString()) + "\"";
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