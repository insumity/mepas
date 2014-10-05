package ch.ethz.inf.asl.exceptions;

public class MessageProtocolException extends RuntimeException {

    public MessageProtocolException() {
        super();
    }

    public MessageProtocolException(String message) {
        super(message);
    }

    public MessageProtocolException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageProtocolException(Throwable cause) {
        super(cause);
    }
}
