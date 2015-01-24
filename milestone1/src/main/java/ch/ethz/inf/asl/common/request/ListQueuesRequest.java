package ch.ethz.inf.asl.common.request;

import ch.ethz.inf.asl.common.MessagingProtocol;
import ch.ethz.inf.asl.common.response.ListQueuesResponse;
import ch.ethz.inf.asl.common.response.Response;
import ch.ethz.inf.asl.exceptions.MessagingProtocolException;

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
        } catch (MessagingProtocolException mpe) {
            return Response.createFailedResponse(mpe.getMessage(), ListQueuesResponse.class);
        }
    }

    @Override
    public String getName() {
        return "LIST_QUEUES";
    }

    @Override
    public String toString() {
        return super.toString() + "(" + getName() + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ListQueuesRequest) {
            ListQueuesRequest other = (ListQueuesRequest) obj;
            return super.equals(other);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
