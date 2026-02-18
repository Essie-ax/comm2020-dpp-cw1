import uk.ac.comm2020.dao.MySqlSubmissionDao;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MySqlSubmissionDaoTest {

    // createSubmission() - should return -1 
    @Test
    public void testReturnsMimusOneWhenNoDB() {
        MySqlSubmissionDao dao = new MySqlSubmissionDao();

        long result = dao.createSubmission(1, 1, 1, 100, "PASS");
        assertEquals(-1, result);
    }

    // getSubmissionById() - should return null when there is no database
    @Test
    public void testGetSubmissionByIdReturnsNullWhenNoDb() {
        MySqlSubmissionDao dao = new MySqlSubmissionDao();

        Map<String, Object> result = dao.getSubmissionById(1);
        assertNull(result);
    }

    // getSubmissionsByChallenge() - should return an empty list when there is no database
    @Test
    public void testGetSubmissionsByChallengeReturnsEmptyListWhenNoDb() {
        MySqlSubmissionDao dao = new MySqlSubmissionDao();

        List<Map<String, Object>> result = dao.getSubmissionsByChallenge(1);

        assertNotNull(result); 
        assertTrue(result.isEmpty()); 
    }
}