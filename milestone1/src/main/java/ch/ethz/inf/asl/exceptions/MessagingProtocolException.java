package ch.ethz.inf.asl.exceptions;

/**
 * Exception class to be used with protocol related exceptions.
 *
 * @see ch.ethz.inf.asl.common.MessagingProtocol
 */
public class MessagingProtocolException extends RuntimeException {

    public MessagingProtocolException() {
        super();
    }

    public MessagingProtocolException(String message) {
        super(message);
    }

    public MessagingProtocolException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessagingProtocolException(Throwable cause) {
        super(cause);
    }
}
