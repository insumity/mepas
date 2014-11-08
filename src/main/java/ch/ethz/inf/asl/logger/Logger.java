package ch.ethz.inf.asl.logger;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import static ch.ethz.inf.asl.utils.Verifier.hasText;

public class Logger {
    private java.util.logging.Logger logger;

    private long startingTime;

    // for emptyLogger
    protected Logger() {

    }

    public Logger(String filePath) throws IOException {
        hasText(filePath, "Given filePath cannot be empty!");

        logger = java.util.logging.Logger.getLogger(filePath);
        logger.setUseParentHandlers(false);

        FileHandler handler = new FileHandler(filePath, true);
        handler.setLevel(Level.INFO);
        handler.setFormatter(new Formatter());
        logger.addHandler(handler);

        startingTime = System.currentTimeMillis();
    }

    public void close() {
        for (Handler handler : this.logger.getHandlers()) {
            handler.flush();
            handler.close();
        }
    }

    private class Formatter extends java.util.logging.Formatter {

        @Override
        public String format(LogRecord record) {
            return record.getMessage() + "\n";
        }
    }


    public void log(String message) {
        StringBuilder builder = new StringBuilder();

        long currentTime = System.currentTimeMillis() - startingTime;
        builder.append(currentTime);
        builder.append('\t');
        builder.append(message);

        logger.info(builder.toString());
    }

}
