package ch.ethz.inf.asl.utils;

import org.testng.annotations.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static ch.ethz.inf.asl.testutils.TestConstants.SMALL;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ConfigurationReaderTest {

    @Test(groups = SMALL, expectedExceptions = NullPointerException.class)
    public void testConfigurationReaderWithNullPath() {
        new ConfigurationReader(null);
    }

    @Test(groups = SMALL, expectedExceptions = IllegalArgumentException.class)
    public void testConfigurationReaderWithEmptyPath() {
        new ConfigurationReader("");
    }

    @Test(groups = SMALL)
    public void testGetProperty() throws IOException {

        // create a configuration file
        File file = File.createTempFile("temp", "", new File("/tmp"));
        String filePath = file.getPath();
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));

        writer.write("some=value is nice to be here\n");
        writer.write("this=234234234");
        writer.close();

        ConfigurationReader reader = new ConfigurationReader(filePath);
        assertEquals(reader.getProperty("some"), "value is nice to be here");
        assertEquals(reader.getProperty("this"), "234234234");
        assertTrue(new File(filePath).delete());
    }
}
