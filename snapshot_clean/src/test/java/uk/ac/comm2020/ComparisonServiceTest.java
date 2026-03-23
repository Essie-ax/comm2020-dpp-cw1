package uk.ac.comm2020;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.comm2020.dao.InMemoryPassportDao;
import uk.ac.comm2020.service.ComparisonService;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ComparisonServiceTest {

    private ComparisonService service;

    @BeforeEach
    void setUp() {
        service = new ComparisonService(new InMemoryPassportDao());
    }

    @Test
    void compareTwoPassportsReturnsResult() throws SQLException {
        Map<String, Object> result = service.compare(1, 2);
        assertNotNull(result.get("passport1"));
        assertNotNull(result.get("passport2"));
        assertNotNull(result.get("fieldDiffs"));
        assertNotNull(result.get("scoreDiff"));
    }

    @Test
    void compareWithMissingPassportThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> service.compare(1, 9999));
        assertThrows(IllegalArgumentException.class, () -> service.compare(9999, 1));
    }

    @Test
    void fieldDiffsContainKeysFromBothPassports() throws SQLException {
        Map<String, Object> result = service.compare(1, 2);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> diffs = (List<Map<String, Object>>) result.get("fieldDiffs");
        assertFalse(diffs.isEmpty());
        for (Map<String, Object> row : diffs) {
            assertTrue(row.containsKey("key"));
            assertTrue(row.containsKey("value1"));
            assertTrue(row.containsKey("value2"));
            assertTrue(row.containsKey("match"));
        }
    }

    @Test
    void fieldDiffsMismatchesComesBeforeMatches() throws SQLException {
        Map<String, Object> result = service.compare(1, 2);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> diffs = (List<Map<String, Object>>) result.get("fieldDiffs");

        boolean seenMatch = false;
        for (Map<String, Object> row : diffs) {
            boolean match = (boolean) row.get("match");
            if (match) {
                seenMatch = true;
            } else {
                // Once we see a match row, any subsequent mismatch means the sort is broken.
                assertFalse(seenMatch, "A mismatch appeared after a match - order is wrong");
            }
        }
    }

    @Test
    void scoreDiffHasCompletenessAndConfidence() throws SQLException {
        Map<String, Object> result = service.compare(1, 2);
        @SuppressWarnings("unchecked")
        Map<String, Object> scoreDiff = (Map<String, Object>) result.get("scoreDiff");
        assertTrue(scoreDiff.containsKey("completeness"));
        assertTrue(scoreDiff.containsKey("confidence"));
    }

    @Test
    void compareSamePassportShowsAllFieldsMatching() throws SQLException {
        Map<String, Object> result = service.compare(1, 1);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> diffs = (List<Map<String, Object>>) result.get("fieldDiffs");

        for (Map<String, Object> row : diffs) {
            assertTrue((boolean) row.get("match"), "Expected all fields to match when comparing passport to itself");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> scoreDiff = (Map<String, Object>) result.get("scoreDiff");
        @SuppressWarnings("unchecked")
        Map<String, Object> completeness = (Map<String, Object>) scoreDiff.get("completeness");
        assertEquals(0.0, completeness.get("diff"));
    }
}
