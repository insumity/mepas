package ch.ethz.inf.asl.common.request;

import ch.ethz.inf.asl.common.MessagingProtocol;
import ch.ethz.inf.asl.common.response.CreateQueueResponse;

public class CreateQueueRequest extends Request {

    private String queueName;

    public CreateQueueRequest(int requestorId, String queueName) {
        super(requestorId);
        this.queueName = queueName;
    }

    @Override
    public CreateQueueResponse execute(MessagingProtocol protocol) {
        int queueId = protocol.createQueue(queueName);
        return new CreateQueueResponse(queueId);
    }

    @Override
    public String toString() {
        return "(CREATE_QUEUE: " + queueName + ")";
    }
}
