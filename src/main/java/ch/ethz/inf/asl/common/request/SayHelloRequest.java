package ch.ethz.inf.asl.common.request;

import ch.ethz.inf.asl.common.MessageConstants;
import ch.ethz.inf.asl.common.MessagingProtocol;
import ch.ethz.inf.asl.common.response.Response;
import ch.ethz.inf.asl.common.response.SayHelloResponse;
import ch.ethz.inf.asl.exceptions.MessagingProtocolException;

import java.util.Objects;

import static ch.ethz.inf.asl.utils.Verifier.notNull;

public class SayHelloRequest extends Request<SayHelloResponse> {

    private String clientName;

    public SayHelloRequest(String clientName) {
        notNull(clientName, "Given clientName cannot be null!");

        if (clientName.length() > MessageConstants.MAXIMUM_CLIENT_NAME_LENGTH) {
            throw new IllegalArgumentException("clientName exceed max queue name length of: "
                    + MessageConstants.MAXIMUM_CLIENT_NAME_LENGTH);
        }

        this.clientName = clientName;
    }

    @Override
    public SayHelloResponse execute(MessagingProtocol protocol) {
        notNull(protocol, "Given protocol cannot be null!");

        try {
            int clientId = protocol.sayHello(clientName);
            return new SayHelloResponse(clientId);
        } catch (MessagingProtocolException mpe) {
            return Response.createFailedResponse(mpe.getMessage(), SayHelloResponse.class);
        }
    }

    @Override
    public String toString() {
        return super.toString() +
                String.format("(SAY_HELLO: [clientName: %s])", clientName);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SayHelloRequest) {
            SayHelloRequest other = (SayHelloRequest) obj;
            return super.equals(other) && Objects.equals(this.clientName, other.clientName);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), clientName);
    }
}
