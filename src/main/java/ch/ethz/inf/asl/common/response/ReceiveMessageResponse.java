package ch.ethz.inf.asl.common.response;

import ch.ethz.inf.asl.common.Message;

public class ReceiveMessageResponse extends GetMessageResponse {

    public ReceiveMessageResponse() {
        super();
    }

    public ReceiveMessageResponse(Message message) {
        super(message);
    }

    @Override
    public String toString() {
        if (!isSuccessful()) {
            return String.format("(RECEIVE_MESSAGE FAILED: %s)", getFailedMessage());
        }

        // the request was successful but there are two cases: a message
        // was received or a message wasn't received
        Message message = getMessage();
        if (message != null) {
            return String.format("(RECEIVE_MESSAGE SUCCESS: %s)", message);
        }
        else {
            return "(RECEIVE_MESSAGE SUCCESS: NO MESSAGE)";
        }
    }

}
