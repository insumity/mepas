package ch.ethz.inf.asl.logger;

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

    public static void main(String[] args) {
        Logger logger = null;
        try {
            logger = new Logger("/tmp/foo.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        long start = System.currentTimeMillis();
        int size = 1000000;
        for (int i = 0; i < size; ++i) {
            logger.log("skata mer igan");
        }
        long end = System.currentTimeMillis();
        System.out.println((end - start) / ((float) size));
    }

}
