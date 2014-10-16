package ch.ethz.inf.asl.common.response;

public class SayHelloResponse extends Response {

    private int clientId;

    protected SayHelloResponse() {}

    // needed for creating failedResponse in Response class
    public SayHelloResponse(int clientId) {
        this.clientId = clientId;
    }

    public int getClientId() {
        return clientId;
    }

    @Override
    public String toString() {
        if (!isSuccessful()) {
            return String.format("(SAY_HELLO FAILED: %s)", getFailedMessage());
        }

        return String.format("(SAY_HELLO SUCCESS: %d)", clientId);
    }
}
