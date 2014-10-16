package ch.ethz.inf.asl.common.request;

import ch.ethz.inf.asl.common.MessagingProtocol;
import ch.ethz.inf.asl.common.response.CreateQueueResponse;
import ch.ethz.inf.asl.common.response.Response;
import ch.ethz.inf.asl.exceptions.MessageProtocolException;

import static ch.ethz.inf.asl.utils.Verifier.notNull;

public class CreateQueueRequest extends Request<CreateQueueResponse> {

    private String queueName;

    public CreateQueueRequest(int requestorId, String queueName) {
        super(requestorId);
        notNull(queueName, "Given queueName cannot be null!s");

        this.queueName = queueName;
    }

    @Override
    public CreateQueueResponse execute(MessagingProtocol protocol) {
        notNull(protocol, "Given protocol cannot be null!");

        try {
            int queueId = protocol.createQueue(queueName);
            return new CreateQueueResponse(queueId);
        } catch (MessageProtocolException mpe) {
            return Response.createFailedResponse(mpe.getMessage(), CreateQueueResponse.class);
        }
    }

    @Override
    public String toString() {
        return super.toString() +
                String.format("(CREATE_QUEUE: [queueName: %s])",  queueName);
    }
}
