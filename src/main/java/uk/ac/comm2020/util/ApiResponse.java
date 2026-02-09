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
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(JsonUtil.escape(entry.getKey())).append("\":");
            sb.append(valueToJson(entry.getValue()));
        }
        sb.append("}");
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private String valueToJson(Object value) {
        if (value == null) return "null";
        if (value instanceof String) return "\"" + JsonUtil.escape((String) value) + "\"";
        if (value instanceof Map) return mapToJson((Map<String, Object>) value);
        if (value instanceof Object[]) {
            StringBuilder sb = new StringBuilder("[");
            Object[] arr = (Object[]) value;
            for (int i = 0; i < arr.length; i++) {
                if (i > 0) sb.append(",");
                sb.append(valueToJson(arr[i]));
            }
            sb.append("]");
            return sb.toString();
        }
        return String.valueOf(value);
    }

    public int getStatus() {
        if (success) return 200;
        String code = (String) error.get("code");
        if ("UNAUTHORIZED".equals(code)) return 401;
        if ("FORBIDDEN".equals(code)) return 403;
        if ("NOT_FOUND".equals(code)) return 404;
        if ("BAD_REQUEST".equals(code)) return 400;
        if ("METHOD_NOT_ALLOWED".equals(code)) return 405;
        return 500;
    }

    public boolean isSuccess() {
        return success;
    }

    public Map<String, Object> getData() {
        return data;
    }
}