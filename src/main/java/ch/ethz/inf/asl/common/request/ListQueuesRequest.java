package ch.ethz.inf.asl.common.request;

import ch.ethz.inf.asl.common.MessagingProtocol;
import ch.ethz.inf.asl.common.response.ListQueuesResponse;
import ch.ethz.inf.asl.common.response.Response;
import ch.ethz.inf.asl.exceptions.MessageProtocolException;

import static ch.ethz.inf.asl.utils.Verifier.notNull;

public class ListQueuesRequest extends Request<ListQueuesResponse> {

    public ListQueuesRequest(int requestorId) {
        super(requestorId);
    }

    @Override
    public ListQueuesResponse execute(MessagingProtocol protocol) {
        notNull(protocol, "Given protocol cannot be null!");

        try {
            return new ListQueuesResponse(protocol.listQueues());
        } catch (MessageProtocolException mpe) {
            return Response.createFailedResponse(mpe.getMessage(), ListQueuesResponse.class);
        }
    }

    @Override
    public String toString() {
        return super.toString() + "(LIST_QUEUES)";
    }
}
