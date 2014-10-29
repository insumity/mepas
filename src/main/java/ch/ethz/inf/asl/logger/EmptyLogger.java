package ch.ethz.inf.asl.logger;

import java.io.IOException;

/** Dummy logger class to be used by end-to-end and so avoid logging stuff while testing. */
public class EmptyLogger extends Logger {
    public EmptyLogger() throws IOException {
        super();
    }

    @Override
    public void close() {
    }


    @Override
    public void log(String message) {
    }
}
