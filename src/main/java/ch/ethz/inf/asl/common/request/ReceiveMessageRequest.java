package ch.ethz.inf.asl.common.request;

import ch.ethz.inf.asl.common.Message;
import ch.ethz.inf.asl.common.MessagingProtocol;
import ch.ethz.inf.asl.common.response.ReceiveMessageResponse;
import ch.ethz.inf.asl.common.response.Response;
import ch.ethz.inf.asl.exceptions.MessageProtocolException;
import ch.ethz.inf.asl.utils.Optional;

import static ch.ethz.inf.asl.utils.Verifier.notNull;

public class ReceiveMessageRequest extends Request<ReceiveMessageResponse> {

    private Integer senderId;
    private int queueId;
    private boolean retrieveByArrivalTime;

    public ReceiveMessageRequest(int requestorId, int queueId, boolean retrieveByArrivalTime) {
        super(requestorId);
        this.senderId = null;
        this.queueId = queueId;
        this.retrieveByArrivalTime = retrieveByArrivalTime;
    }

    public ReceiveMessageRequest(int requestorId, int senderId, int queueId, boolean retrieveByArrivalTime) {
        this(requestorId, queueId, retrieveByArrivalTime);
        this.senderId = senderId;
    }

    private boolean receiveMessageFromSender() {
        return senderId != null;
    }

    @Override
    public ReceiveMessageResponse execute(MessagingProtocol protocol) {
        notNull(protocol, "Given protocol cannot be null!");

        try {
            Optional<Message> message;
            if (receiveMessageFromSender()) {
                message = protocol.receiveMessage(senderId, queueId, retrieveByArrivalTime);
            }
            else {
                message = protocol.receiveMessage(queueId, retrieveByArrivalTime);
            }

            // check if there was a message in the received optional
            if (message.isPresent()) {
                return new ReceiveMessageResponse(message.get());
            }
            else {
                return new ReceiveMessageResponse();
            }
        }
        catch (MessageProtocolException mpe) {
            return Response.createFailedResponse(mpe.getMessage(), ReceiveMessageResponse.class);
        }
    }

    @Override
    public String toString() {
        return super.toString()
                + String.format("(RECEIVE_MESSAGE: [senderId: %s], [queueId:%d], [retrieveByArrivalTime: %b])",
                    senderId, queueId, retrieveByArrivalTime);
    }
}
