package ch.ethz.inf.asl.common.response;

import ch.ethz.inf.asl.common.Message;

public class ReadMessageResponse extends GetMessageResponse {

    public ReadMessageResponse() {
        super();
    }

    public ReadMessageResponse(Message message) {
        super(message);
    }

    @Override
    public String toString() {
        if (!isSuccessful()) {
            return String.format("(READ_MESSAGE FAILED: %s)", getFailedMessage());
        }

        // the request was successful but there are two cases: a message
        // was received or a message wasn't received
        Message message = getMessage();
        if (message != null) {
            return String.format("(READ_MESSAGE SUCCESS: %s)", message);
        }
        else {
            return "(READ_MESSAGE SUCCESS: NO MESSAGE)";
        }
    }
}
