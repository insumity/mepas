package ch.ethz.inf.asl.common.response;

import java.io.Serializable;
import java.util.Objects;

/**
 * This class represents a general Response object that can be either successful or not.
 */
public abstract class Response implements Serializable {

    private static final long serialVersionUID = 2L;

    // shows whether the request corresponding to this response was
    // successful or not
    private boolean isSuccessful;
    private String failedMessage;

    public Response() {
        this.isSuccessful = true;
    }

    /**
     * Returns a failed response with the given message and of the given type.
     * @param message response's fail message
     * @param responseType the type of the response
     * @param <R> returned type of the response
     * @return returns a response of the given type that is a failed one
     */
    public static <R extends Response> R createFailedResponse(String message, Class<R> responseType) {
        Response response = null;
        try {
            response = responseType.newInstance();
            response.isSuccessful = false;
            response.failedMessage = message;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return (R) response;
    }

    /**
     * Returns whether this response is a successful one or not.
     * @return true if the response is successful, false otherwise
     */
    public boolean isSuccessful() {
        return isSuccessful;
    }

    /**
     * In case of a failed response, this method returns the failed message of the response.
     * @return failed message
     */
    public String getFailedMessage() {
        return failedMessage;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Response) {
            Response other = (Response) obj;
            return this.isSuccessful == other.isSuccessful && Objects.equals(this.failedMessage, other.failedMessage);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(isSuccessful, failedMessage);
    }
}
