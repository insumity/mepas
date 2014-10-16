package ch.ethz.inf.asl.common.response;

import ch.ethz.inf.asl.common.Message;

import static ch.ethz.inf.asl.utils.Verifier.notNull;

public abstract class GetMessageResponse extends Response {
    private Message message;

    // in the case of no message received from the request
    public GetMessageResponse() {
        message = null;
    }

    public GetMessageResponse(Message message) {
        notNull(message, "Given message cannot be null!");
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }
}
