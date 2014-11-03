package ch.ethz.inf.asl.logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.*;

public class Logger {
    private java.util.logging.Logger logger;

    private long startingTime;

    // for emptyLogger
    protected Logger() {

    }

    public Logger(String fileName) throws IOException {
        logger = java.util.logging.Logger.getLogger(fileName);
        logger.setUseParentHandlers(false);

        FileHandler handler = new FileHandler(fileName, true);
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
