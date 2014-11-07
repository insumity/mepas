package ch.ethz.inf.asl.logger;

import org.testng.annotations.Test;

import java.io.*;
import java.nio.file.Files;

import static ch.ethz.inf.asl.testutils.TestConstants.SMALL;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class LoggerTest {

    @Test(groups = SMALL, expectedExceptions = IllegalArgumentException.class)
    public void testCannotCreateLoggerWithEmptyPath() throws IOException {
        new Logger("");
    }

    @Test(groups = SMALL, expectedExceptions = NullPointerException.class)
    public void testCannotCreateLoggerWithNullPath() throws IOException {
        new Logger(null);
    }

    @Test(groups = SMALL)
    public void testLog() throws IOException {

        File file = File.createTempFile("temp", "", new File("/tmp"));
        String filePath = file.getPath();
        Logger logger = new Logger(filePath);

        String firstLine = "some data";
        String secondLine = "some more data";
        logger.log(firstLine);
        logger.log(secondLine);
        logger.close();

        BufferedReader reader = new BufferedReader(new FileReader(filePath));

        assertTrue(reader.readLine().contains(firstLine));
        assertTrue(reader.readLine().contains(secondLine));
        assertNull(reader.readLine());

        assertTrue(new File(filePath).delete());
    }
}
