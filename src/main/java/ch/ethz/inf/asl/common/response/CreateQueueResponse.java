package ch.ethz.inf.asl.common.response;

public class CreateQueueResponse extends Response {

    private int queueId;

    protected CreateQueueResponse() {}

    // needed for creating failedResponse in Response class
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
}
