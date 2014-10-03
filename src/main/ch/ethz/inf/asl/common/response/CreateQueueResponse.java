package ch.ethz.inf.asl.common.response;

public class CreateQueueResponse extends Response {

    private int queueId;

    public CreateQueueResponse(int queueId) {
        this.queueId = queueId;
    }

    public int getQueueId() {
        return queueId;
    }

    @Override
    public String toString() {
        return "(CREATE_QUEUE: " + queueId + ")";
    }
}
