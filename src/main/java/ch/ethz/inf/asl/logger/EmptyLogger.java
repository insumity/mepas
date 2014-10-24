package ch.ethz.inf.asl.logger;

import java.io.IOException;

/** Dummy logger class to be used by end-to-end and so avoid logging stuff while testing. */
public class EmptyLogger extends Logger {
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
