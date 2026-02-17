package uk.ac.comm2020.config.EnvConfig;

import org.junit.juipter.api.Test; 
import org.junit.jupiter.api.Assertions.*;

import java.beans.Transient;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;





public class EnvConfigTest {

    // get() -  returns default is the key is missing
    public void testGetWrithDefaultValue() {
        EnvConfig config = EnvConfig.load();

        String value = config.get("letsHopeThisKeyDoesntExistOrElseUhOh....", "default123");
        assertEquals("default123", value);
    }

    // get() - File should load strings
    public void testEnvFileLoadsValues() throws IOException {
         Path envFile = Path.of(".env");

        Files.writeString(envFile, "MY_KEY=hello_world");

        EnvConfig config = EnvConfig.load();
        string value = config.get("MY_KEY", "fallback");
        assertEquals("hello_world", value);

        Files.deleteIfExists(envFile);
    }

    // require() - Should throw an exception if key is missing
    public void testRequireThrowsWhenMisssing() {
        EnvConfig config = EnvConfig.load();

        assertThrows(IllegalStateException.class, () -> {
            config.require("missingKey");
        });
    }
    
    // require() - should work when value exists in .env
     public void testRequireWorksWhenValuePresent() throws IOException {
        Path envFile = Path.of(".env");

        Files.writeString(envFile, "REQ_KEY=testValue");

        EnvConfig config = EnvConfig.load();
        string value = config.get("REQ_KEY", "fallback");
        assertEquals("testValue", value);

        Files.deleteIfExists(envFile);
    }

    // getInt() - returns default is the key is missing
    public void testGetIntDefaultWheMissing() {
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