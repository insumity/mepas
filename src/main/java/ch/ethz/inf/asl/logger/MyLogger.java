package ch.ethz.inf.asl.logger;

import java.io.IOException;
import java.util.logging.*;


public class MyLogger {
    private Logger logger;

    public MyLogger(String fileName) throws IOException {
        logger = Logger.getLogger(fileName);
        logger.setUseParentHandlers(false);

        FileHandler handler = new FileHandler(fileName, true);
        handler.setLevel(Level.INFO);
        handler.setFormatter(new MyFormatter());
        logger.addHandler(handler);
    }

    public void close() {
        for (Handler handler: this.logger.getHandlers()) {
            handler.close();
        }
    }

    private class MyFormatter extends Formatter {

        @Override
        public String format(LogRecord record) {
            return record.getMessage() + "\n";
        }
    }


    public void log(long time, String message) {
        logger.severe(String.format("%d\t%s", time, message));
        logger.getHandlers()[0].flush();
    }

    public synchronized void synchronizedLog(long time, String message) {
        logger.severe(String.format("%d\t%s", time, message));
        logger.getHandlers()[0].flush();
    }
}
