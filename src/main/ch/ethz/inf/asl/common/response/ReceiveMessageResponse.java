package ch.ethz.inf.asl.common.response;

import ch.ethz.inf.asl.common.Message;
import ch.ethz.inf.asl.utils.Optional;

public class ReceiveMessageResponse extends Response {

    private Message message;

    public ReceiveMessageResponse(Optional<Message> message) {
        if (message.isPresent()) {
            this.message = message.get();
        }
        else {
            this.message = null;
        }
    }

    // FIXME .. should return OPTIONAL MESSAGE?
    public Message getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "(RECEIVE_MESSAGE: " + String.valueOf(message) + ")";
    }
}
