package ch.ethz.inf.asl.common.request;

import ch.ethz.inf.asl.common.MessagingProtocol;
import ch.ethz.inf.asl.common.response.Response;
import ch.ethz.inf.asl.common.response.SayHelloResponse;
import ch.ethz.inf.asl.exceptions.MessageProtocolException;

import static ch.ethz.inf.asl.utils.Verifier.notNull;

public class SayHelloRequest extends Request<SayHelloResponse> {

    private String clientName;

    public SayHelloRequest(String clientName) {
        notNull(clientName, "Given clientName cannot be null!s");

        this.clientName = clientName;
    }

    @Override
    public SayHelloResponse execute(MessagingProtocol protocol) {
        notNull(protocol, "Given protocol cannot be null!");

        try {
//            int clientId = protocol.sayHello(clientName);
            return new SayHelloResponse(-1);
        } catch (MessageProtocolException mpe) {
            return Response.createFailedResponse(mpe.getMessage(), SayHelloResponse.class);
        }
    }

    @Override
    public String toString() {
        return super.toString() +
                String.format("(SAY_HELLO: [clientName: %s])", clientName);
    }
}
