package ch.ethz.inf.asl.common.request;

import ch.ethz.inf.asl.common.MessagingProtocol;
import ch.ethz.inf.asl.common.response.DeleteQueueResponse;
import ch.ethz.inf.asl.common.response.Response;
import ch.ethz.inf.asl.exceptions.MessagingProtocolException;

import java.util.Objects;

import static ch.ethz.inf.asl.utils.Verifier.notNull;

public class DeleteQueueRequest extends Request<DeleteQueueResponse> {

    private int queueId;

    public DeleteQueueRequest(int requestorId, int queueId) {
        super(requestorId);
        this.queueId = queueId;
    }

    @Override
    public DeleteQueueResponse execute(MessagingProtocol protocol) {
        notNull(protocol, "Given protocol cannot be null!");

        try {
            protocol.deleteQueue(queueId);
            return new DeleteQueueResponse();
        } catch (MessagingProtocolException mpe) {
            return Response.createFailedResponse(mpe.getMessage(), DeleteQueueResponse.class);
        }
    }

    @Override
    public String getName() {
        return "DELETE_QUEUE";
    }

    @Override
    public String toString() {
        return super.toString() +
                String.format("(%s: [queueId: %s])",  getName(), queueId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DeleteQueueRequest) {
            DeleteQueueRequest other = (DeleteQueueRequest) obj;
            return super.equals(other) && Objects.equals(this.queueId, other.queueId);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), queueId);
    }

}
