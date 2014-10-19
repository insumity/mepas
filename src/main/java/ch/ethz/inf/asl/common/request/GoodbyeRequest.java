package ch.ethz.inf.asl.common.request;

import ch.ethz.inf.asl.common.MessagingProtocol;
import ch.ethz.inf.asl.common.response.GoodbyeResponse;
import ch.ethz.inf.asl.common.response.Response;
import ch.ethz.inf.asl.exceptions.MessageProtocolException;

import static ch.ethz.inf.asl.utils.Verifier.notNull;

public class GoodbyeRequest extends Request<GoodbyeResponse> {
    public GoodbyeRequest(int requestorId) {
        super(requestorId);
    }

    @Override
    public GoodbyeResponse execute(MessagingProtocol protocol) {
        notNull(protocol, "Given protocol cannot be null!");

        try {
//            protocol.sayGoodbye();
            return new GoodbyeResponse();
        } catch (MessageProtocolException mpe) {
            return Response.createFailedResponse(mpe.getMessage(), GoodbyeResponse.class);
        }
    }

    @Override
    public String toString() {
        return super.toString() + "(GOODBYE)";
    }
}
