package ch.ethz.inf.asl.common.request;

import ch.ethz.inf.asl.common.MessagingProtocol;
import ch.ethz.inf.asl.common.response.GoodbyeResponse;

public class GoodbyeRequest extends Request {
    public GoodbyeRequest(int requestorId) {
        super(requestorId);
    }

    @Override
    public GoodbyeResponse execute(MessagingProtocol protocol) {
        return new GoodbyeResponse();
    }
}
