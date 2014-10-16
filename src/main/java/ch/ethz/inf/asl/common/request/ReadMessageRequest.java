package ch.ethz.inf.asl.common.request;

import ch.ethz.inf.asl.common.Message;
import ch.ethz.inf.asl.common.MessagingProtocol;
import ch.ethz.inf.asl.common.response.ReadMessageResponse;
import ch.ethz.inf.asl.common.response.ReceiveMessageResponse;
import ch.ethz.inf.asl.common.response.Response;
import ch.ethz.inf.asl.exceptions.MessageProtocolException;
import ch.ethz.inf.asl.utils.Optional;

import static ch.ethz.inf.asl.utils.Verifier.notNull;

public class ReadMessageRequest extends Request<ReadMessageResponse> {

    private int queueId;
    private boolean retrieveByArrivalTime;

    public ReadMessageRequest(int requestorId, int queueId, boolean retrieveByArrivalTime) {
        super(requestorId);
        this.queueId = queueId;
        this.retrieveByArrivalTime = retrieveByArrivalTime;
    }

    @Override
    public ReadMessageResponse execute(MessagingProtocol protocol) {
        notNull(protocol, "Given protocol cannot be null!");

        try {
            Optional<Message> message = protocol.readMessage(queueId, retrieveByArrivalTime);

            // check if there was a message in the received optional
            if (message.isPresent()) {
                return new ReadMessageResponse(message.get());
            }
            else {
                return new ReadMessageResponse();
            }
        } catch (MessageProtocolException mpe) {
            return Response.createFailedResponse(mpe.getMessage(), ReadMessageResponse.class);
        }
    }

    @Override
    public String toString() {
        return super.toString() + String.format("(READ_MESSAGE: [queueId: %d],  [retrieveByArrivalTime: %b])",
                queueId, retrieveByArrivalTime);
    }

}
