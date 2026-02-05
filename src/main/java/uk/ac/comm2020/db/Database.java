package uk.ac.comm2020.db;

import uk.ac.comm2020.config.EnvConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// DB helper. Make JDBC connection from env config.
public class Database {
    private final EnvConfig config;

    // keep config, and load mysql driver once
    public Database(EnvConfig config) {
        this.config = config;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            // without driver, nothing can work
            throw new IllegalStateException("MySQL driver not found", e);
        }
    }

    // Get a new DB connection.
    public Connection getConnection() throws SQLException {
        // read db info from env, if missing then use default
        String host = config.get("DB_HOST", "localhost");
        String port = config.get("DB_PORT", "3306");
        String name = config.get("DB_NAME", "comm2020_dpp");
        String user = config.get("DB_USER", "comm2020");
        String password = config.get("DB_PASSWORD", "comm2020_password");

        // build jdbc url
        String url = String.format(
                "jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                host,
                port,
                name
        );

        // open connection and return it
        return DriverManager.getConnection(url, user, password);
    }
}
