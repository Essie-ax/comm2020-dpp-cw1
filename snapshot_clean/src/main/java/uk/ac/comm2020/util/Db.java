package uk.ac.comm2020.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public final class Db {

    private static final Map<String, String> envFromFile = new HashMap<>();

    static {
        loadEnvFile();
    }

    private Db() {}

    /**
     * Load .env from current working directory (project root when run from IDE or mvn).
     * Lines: KEY=value or KEY="value". Comments (#) and empty lines skipped.
     */
    private static void loadEnvFile() {
        Path path = Paths.get(System.getProperty("user.dir", ".")).resolve(".env");
        if (!Files.isRegularFile(path)) return;
        try {
            for (String line : Files.readAllLines(path)) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int eq = line.indexOf('=');
                if (eq <= 0) continue;
                String key = line.substring(0, eq).trim();
                String value = line.substring(eq + 1).trim();
                if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2)
                    value = value.substring(1, value.length() - 1);
                envFromFile.put(key, value);
            }
        } catch (IOException ignored) {
            // .env missing or unreadable: rely on system env only
        }
    }

    public static String getEnv(String name) {
        String v = envFromFile.get(name);
        if (v != null) return v.trim();
        v = System.getenv(name);
        return (v == null) ? "" : v.trim();
    }

    public static boolean hasUrl() {
        String url = getEnv("DB_URL");
        return url != null && !url.isBlank();
    }

    /** Try to open and close a connection. Returns true if DB is reachable, false otherwise. */
    public static boolean tryConnection() {
        try (Connection c = getConnection()) {
            return c != null;
        } catch (SQLException e) {
            return false;
        }
    }

    public static Connection getConnection() throws SQLException {
        String url = getEnv("DB_URL");
        if (url == null || url.isBlank()) return null;
        String user = getEnv("DB_USER");
        String pass = getEnv("DB_PASS");

        if ((user == null || user.isEmpty()) && (pass == null || pass.isEmpty())) {
            return DriverManager.getConnection(url);
        }
        return DriverManager.getConnection(url, user, pass);
    }
}
