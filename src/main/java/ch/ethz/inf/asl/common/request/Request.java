package ch.ethz.inf.asl.common.request;

import ch.ethz.inf.asl.common.MessagingProtocol;
import ch.ethz.inf.asl.common.response.Response;

import java.io.Serializable;
import java.util.Objects;

public abstract class Request<R extends Response> implements Serializable {

    private static final long serialVersionUID = 1L;

    private int requestorId;

    // needed for SayHelloRequest, the requestorId is not known yet!
    // FIXME
    protected Request() { }

    public Request(int requestorId) {
        this.requestorId = requestorId;
    }

    public int getRequestorId() {
        return requestorId;
    }

    public abstract R execute(MessagingProtocol protocol);

    @Override
    public String toString() {
        return String.format("(requestorId %d)", requestorId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Request) {
            Request other = (Request) obj;
            return this.requestorId == other.requestorId;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestorId);
    }
}
