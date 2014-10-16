package ch.ethz.inf.asl.common.response;

import java.io.Serializable;
import java.util.Objects;

public class Response implements Serializable {

    private static final long serialVersionUID = 2L;

    // shows whether the request corresponding to this response was
    // successful or not
    private boolean isSuccessful;
    private String failedMessage;

    public Response() {
        this.isSuccessful = true;
    }

    // FIXME: what is this shit? Have you ever wondered in the darkness of your code?
    // TODO: Have you ever felt the triumph of bad code over decency? Or is it just me?
    // This code is way to generic? But at the end, does it really matter? FIXME
    public static <R extends Response> R createFailedResponse(String message, Class<R> responseType) {
        Response response = null;
        try {
            response = responseType.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        response.isSuccessful = false;
        response.failedMessage = message;
        return (R) response;
    }

    // FIXME TODO add java docs whether request was successful
    public boolean isSuccessful() {
        return isSuccessful;
    }

    // IS going to be empty if the resonse was successful bla bla FIXME
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
