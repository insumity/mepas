package ch.ethz.inf.asl.common.request;

import ch.ethz.inf.asl.common.MessagingProtocol;
import ch.ethz.inf.asl.common.response.Response;

import java.io.Serializable;
import java.util.Objects;

/**
 * This abstract class represents a general request.
 * @param <R> corresponding response type for this kind of request. For example if
 *           a "foo" request returns a "foo" response, then the "foo" request needs
 *           to be implemented like this:
 *           {@code class FooRequest<FooResponse> {} }
 */
public abstract class Request<R extends Response> implements Serializable {

    private static final long serialVersionUID = 1L;

    private int requestorId;

    // Assigns the given requestor id to the requestor id of this request
    protected Request(int requestorId) {
        this.requestorId = requestorId;
    }

    /**
     * Returns the requestor id of this request, i.e. this is the id of the user that issued the request.
     * @return the requestor id
     */
    public int getRequestorId() {
        return requestorId;
    }

    /**
     * Executed the given request in the given protocol and returns the execution response.
     * @param protocol protocol where the request is being issued upon.
     * @return response of the executed request.
     */
    public abstract R execute(MessagingProtocol protocol);

    /**
     * Gets the name of this type of requests.
     * @return name of this type of request
     */
    public abstract String getName();

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
