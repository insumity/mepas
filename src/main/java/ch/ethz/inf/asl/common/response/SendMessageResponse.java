package ch.ethz.inf.asl.common.response;

public class SendMessageResponse extends Response {

    @Override
    public String toString() {
        if (!isSuccessful()) {
            return String.format("(SEND_MESSAGE FAILED: %s)", getFailedMessage());
        }

        return "(SEND_MESSAGE SUCCESS)";
    }
}
