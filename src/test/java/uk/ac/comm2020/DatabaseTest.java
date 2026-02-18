package uk.ac.comm2020;
import uk.ac.comm2020.db.Database;
import uk.ac.comm2020.config.EnvConfig;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;

public class DatabaseTest {

    // Shouldnt throw is SQL is present
    @Test
    public void testConstrutorLoadsDriver() {
        EnvConfig config = EnvConfig.load();

        Database db = new Database(config);
        assertNotNull(db);
    }

    // getConnection() - should throw SQLException if database is not runing
    @Test
    public void testConnectionNoSQLExc() {
        EnvConfig config = EnvConfig.load();

        Database db = new Database(config);
        assertThrows(SQLException.class, () -> {
            db.getConnection();
        });
    }
}
