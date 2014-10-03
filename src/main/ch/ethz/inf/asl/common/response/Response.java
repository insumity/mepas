package ch.ethz.inf.asl.common.response;

import java.io.Serializable;

public abstract class Response implements Serializable {

    private boolean wasSuccessfull;

    public Response() {
        this.wasSuccessfull = true;
    }

    public Response(boolean wasSuccessfull) {
        this.wasSuccessfull = false;
    }

    // FIXME TODO add java docs whether request was successful
    public boolean wasSuccessfull() {
        return wasSuccessfull;
    }
}
