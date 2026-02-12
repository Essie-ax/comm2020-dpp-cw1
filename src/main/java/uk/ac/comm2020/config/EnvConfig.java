package uk.ac.comm2020.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Simple env config loader.
// Read from system env first, then try .env file.
public class EnvConfig {
    private final Map<String, String> values;

    private EnvConfig(Map<String, String> values) {
        this.values = values;
    }

    // Load config values.
    // If .env exists, it can fill missing keys.
    public static EnvConfig load() {
        // start from real env vars
        Map<String, String> values = new HashMap<>(System.getenv());

        // try read local .env file
        Path envPath = Paths.get(".env");
        if (Files.exists(envPath)) {
            try {
                List<String> lines = Files.readAllLines(envPath, StandardCharsets.UTF_8);

                for (String line : lines) {
                    String trimmed = line.trim();

                    // skip empty line and comment line
                    if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                        continue;
                    }

                    // we only accept "key=value"
                    int idx = trimmed.indexOf('=');
                    if (idx <= 0) {
                        continue;
                    }

                    String key = trimmed.substring(0, idx).trim();
                    String value = trimmed.substring(idx + 1).trim();

                    // remove quotes like "abc"
                    if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
                        value = value.substring(1, value.length() - 1);
                    }

                    // only set when env not already has it
                    if (!values.containsKey(key) || values.get(key) == null || values.get(key).isBlank()) {
                        values.put(key, value);
                    }
                }
            } catch (IOException ignored) {
            }
        }

        return new EnvConfig(values);
    }

    // Get value with default.
    public String get(String key, String defaultValue) {
        return values.getOrDefault(key, defaultValue);
    }

    // Get value, must exist.
    public String require(String key) {
        String value = values.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required env var: " + key);
        }
        return value;
    }

    // Get int value, parse fail then use default.
    public int getInt(String key, int defaultValue) {
        String value = values.get(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
