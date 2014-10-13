package ch.ethz.inf.asl.logger;

import java.io.IOException;
import java.util.logging.*;


public class MyLogger {

    public static void main(String[] args) throws IOException {
        new MyLogger("yo").log(2343214, "DSafadsf");
        new MyLogger("yo").log(2343214, "DSafadsf");
        new MyLogger("yo").log(2343214, "DSafadsf");
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

    private Logger logger;

    public MyLogger(String name) throws IOException {
        logger = Logger.getLogger(name);
        logger.setUseParentHandlers(false);

        FileHandler handler = new FileHandler("logs/" + name + ".log", true);
        handler.setLevel(Level.INFO);
        handler.setFormatter(new MyFormatter());
        logger.addHandler(handler);
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
