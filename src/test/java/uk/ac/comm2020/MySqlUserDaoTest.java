import uk.ac.comm2020.dao.MySqlUserDao;
import uk.ac.comm2020.model.User;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

public class MySqlUserDaoTest {

    // findByUserame() - Should return empty when null is sent
    @Test
    public void testFindByUsernameWithNull() {
        MySqlUserDao dao = new MySqlUserDao();

        Optional<User> result = dao.findByUsername(null);
        assertTrue(result.isEmpty());
    }

    // findByUserame() - Should return empty when a blank string is sent
    @Test
    public void testFindByUsernameWithBlank() {
        MySqlUserDao dao = new MySqlUserDao();

        Optional<User> result = dao.findByUsername("   ");

        assertTrue(result.isEmpty());
    }
}
