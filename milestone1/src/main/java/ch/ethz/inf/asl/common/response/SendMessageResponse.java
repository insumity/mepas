package ch.ethz.inf.asl.common.response;

import java.util.Objects;

public class SendMessageResponse extends Response {

    @Override
    public String toString() {
        if (!isSuccessful()) {
            return String.format("(SEND_MESSAGE FAILED: %s)", getFailedMessage());
        }

        return "(SEND_MESSAGE SUCCESS)";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SendMessageResponse) {
            SendMessageResponse other = (SendMessageResponse) obj;
            return super.equals(other);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }
}
