package uk.ac.comm2020.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// Custom error for service layer.
// Keep code + http status + extra info for client.
public class ServiceException extends RuntimeException {
    private final String code;
    private final int status;
    private final Map<String, Object> details;

    // code is like NOT_FOUND / DATABASE_ERROR, status is http status
    public ServiceException(String code, String message, int status, Map<String, Object> details, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.status = status;

        // make details read-only, so nobody change it later
        this.details = details == null ? null : Collections.unmodifiableMap(details);
    }

    // error code for client
    public String getCode() {
        return code;
    }

    // http status for client
    public int getStatus() {
        return status;
    }

    // extra info, can be null
    public Map<String, Object> getDetails() {
        return details;
    }

    // small helper to make details map fast
    public static Map<String, Object> details(String key, Object value) {
        Map<String, Object> details = new HashMap<>();
        details.put(key, value);
        return details;
    }
}
