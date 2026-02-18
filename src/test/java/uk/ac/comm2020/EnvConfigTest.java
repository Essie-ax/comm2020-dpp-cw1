import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import uk.ac.comm2020.config.EnvConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class EnvConfigTest {

    // get() - returns default if key is missing
    @Test
    public void testGetWithDefaultValue() {
        EnvConfig config = EnvConfig.load();

        String value = config.get("letsHopeThisKeyDoesntExistOrElseUhOh", "default123");

        assertEquals("default123", value);
    }

    // get() - file should load strings
    @Test
    public void testEnvFileLoadsValues() throws IOException {
        Path envFile = Path.of(".env");

        Files.writeString(envFile, "MY_KEY=hello_world");

        EnvConfig config = EnvConfig.load();
        String value = config.get("MY_KEY", "fallback");

        assertEquals("hello_world", value);

        Files.deleteIfExists(envFile);
    }

    // require() - should throw exception if key missing
    @Test
    public void testRequireThrowsWhenMissing() {
        EnvConfig config = EnvConfig.load();

        assertThrows(IllegalStateException.class, () -> {
            config.require("missingKey");
        });
    }

    // require() - should work when value exists in .env
    @Test
    public void testRequireWorksWhenValuePresent() throws IOException {
        Path envFile = Path.of(".env");

        Files.writeString(envFile, "REQ_KEY=testValue");

        EnvConfig config = EnvConfig.load();
        String value = config.require("REQ_KEY");

        assertEquals("testValue", value);

        Files.deleteIfExists(envFile);
    }

    // getInt() - returns default if key missing
    @Test
    public void testGetIntDefaultWhenMissing() {
        EnvConfig config = EnvConfig.load();

        int value = config.getInt("intDoesntExist", 42);

        assertEquals(42, value);
    }

    // getInt() - returns parsed number when valid
    @Test
    public void testGetIntParsesNumber() throws IOException {
        Path envFile = Path.of(".env");

        Files.writeString(envFile, "intTest=123");

        EnvConfig config = EnvConfig.load();
        int value = config.getInt("intTest", 0);

        assertEquals(123, value);

        Files.deleteIfExists(envFile);
    }
}
