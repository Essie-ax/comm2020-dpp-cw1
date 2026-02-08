package uk.ac.comm2020.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Db {

    private Db() {}

    public static String getEnv(String name) {
        String v = System.getenv(name);
        return (v == null) ? "" : v.trim();
    }

    public static boolean hasUrl() {
        String url = getEnv("DB_URL");
        return url != null && !url.isBlank();
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
