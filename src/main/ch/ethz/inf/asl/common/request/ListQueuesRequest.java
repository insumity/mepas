package ch.ethz.inf.asl.common.request;

import ch.ethz.inf.asl.common.MessagingProtocol;
import ch.ethz.inf.asl.common.response.ListQueuesResponse;

public class ListQueuesRequest extends Request {

    public ListQueuesRequest(int requestorId) {
        super(requestorId);
    }

    @Override
    public ListQueuesResponse execute(MessagingProtocol protocol) {
        return new ListQueuesResponse(protocol.listQueues());
    }

    @Override
    public String toString() {
        return "(LIST_QUEUES)";
    }
}
