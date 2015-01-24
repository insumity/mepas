package ch.ethz.inf.asl.common.response;

import java.util.Objects;

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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SayHelloResponse) {
            SayHelloResponse other = (SayHelloResponse) obj;
            return super.equals(other) && Objects.equals(this.clientId, other.clientId);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), clientId);
    }
}
