package ch.ethz.inf.asl.logger;

import java.io.IOException;

public class EmptyLogger extends MyLogger {
    public EmptyLogger() throws IOException {
        super();
    }

    public void close() {
    }


    public void log(long time, String message) {
    }

    public void synchronizedLog(long time, String message) {
    }
}
