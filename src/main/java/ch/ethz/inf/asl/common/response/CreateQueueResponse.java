package ch.ethz.inf.asl.common.response;

import java.util.Objects;

public class CreateQueueResponse extends Response {

    private int queueId;

    // needed for creating failedResponse in Response class
    protected CreateQueueResponse() {}

    public CreateQueueResponse(int queueId) {
        this.queueId = queueId;
    }

    public int getQueueId() {
        return queueId;
    }

    @Override
    public String toString() {
        if (!isSuccessful()) {
            return String.format("(CREATE_QUEUE FAILED: %s)", getFailedMessage());
        }

        return String.format("(CREATE_QUEUE SUCCESS: %d)", queueId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CreateQueueResponse) {
            CreateQueueResponse other = (CreateQueueResponse) obj;
            return super.equals(other) && Objects.equals(this.queueId, other.queueId);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), queueId);
    }
}
