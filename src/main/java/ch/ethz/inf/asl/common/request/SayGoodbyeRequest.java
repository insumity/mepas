package ch.ethz.inf.asl.common.request;

import ch.ethz.inf.asl.common.MessagingProtocol;
import ch.ethz.inf.asl.common.response.SayGoodbyeResponse;
import ch.ethz.inf.asl.common.response.Response;
import ch.ethz.inf.asl.exceptions.MessagingProtocolException;

import static ch.ethz.inf.asl.utils.Verifier.notNull;

public class SayGoodbyeRequest extends Request<SayGoodbyeResponse> {
    public SayGoodbyeRequest(int requestorId) {
        super(requestorId);
    }

    @Override
    public SayGoodbyeResponse execute(MessagingProtocol protocol) {
        notNull(protocol, "Given protocol cannot be null!");

        try {
            protocol.sayGoodbye();
            return new SayGoodbyeResponse();
        } catch (MessagingProtocolException mpe) {
            return Response.createFailedResponse(mpe.getMessage(), SayGoodbyeResponse.class);
        }
    }

    @Override
    public String getName() {
        return "SAY_GOODBYE";
    }

    @Override
    public String toString() {
        return super.toString() + "(" + getName() + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SayGoodbyeRequest) {
            SayGoodbyeRequest other = (SayGoodbyeRequest) obj;
            return super.equals(other);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
