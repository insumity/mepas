package ch.ethz.inf.asl.common.response;

import ch.ethz.inf.asl.common.Message;

import java.util.Objects;

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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GetMessageResponse) {
            GetMessageResponse other = (GetMessageResponse) obj;
            return super.equals(other) && Objects.equals(this.message, other.message);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), message);
    }
}
