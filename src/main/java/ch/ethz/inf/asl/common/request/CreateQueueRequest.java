package ch.ethz.inf.asl.common.request;

import ch.ethz.inf.asl.common.MessageConstants;
import ch.ethz.inf.asl.common.MessagingProtocol;
import ch.ethz.inf.asl.common.response.CreateQueueResponse;
import ch.ethz.inf.asl.common.response.Response;
import ch.ethz.inf.asl.exceptions.MessagingProtocolException;

import java.util.Objects;

import static ch.ethz.inf.asl.utils.Verifier.notNull;

public class CreateQueueRequest extends Request<CreateQueueResponse> {

    private String queueName;

    public CreateQueueRequest(int requestorId, String queueName) {
        super(requestorId);
        notNull(queueName, "Given queueName cannot be null!");
        if (queueName.length() > MessageConstants.MAXIMUM_QUEUE_NAME_LENGTH) {
            throw new IllegalArgumentException("queueName exceeds max queue name length of: "
                    + MessageConstants.MAXIMUM_QUEUE_NAME_LENGTH);
        }


        this.queueName = queueName;
    }


    @Override
    public CreateQueueResponse execute(MessagingProtocol protocol) {
        notNull(protocol, "Given protocol cannot be null!");

        try {
            int queueId = protocol.createQueue(queueName);
            return new CreateQueueResponse(queueId);
        } catch (MessagingProtocolException mpe) {
            return Response.createFailedResponse(mpe.getMessage(), CreateQueueResponse.class);
        }
    }

    @Override
    public String getName() {
        return "CREATE_QUEUE";
    }

    @Override
    public String toString() {
        return super.toString() +
                String.format("(%: [queueName: %s])",  getName(), queueName);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CreateQueueRequest) {
            CreateQueueRequest other = (CreateQueueRequest) obj;
            return super.equals(other) && Objects.equals(this.queueName, other.queueName);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), queueName);
    }

}
