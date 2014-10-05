package ch.ethz.inf.asl.common.request;

import ch.ethz.inf.asl.common.Message;
import ch.ethz.inf.asl.common.MessagingProtocol;
import ch.ethz.inf.asl.common.response.ReceiveMessageResponse;
import ch.ethz.inf.asl.utils.Optional;

public class ReceiveMessageRequest extends Request {

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
        super(requestorId); // FIXME duplicate code in the constructors
        this.senderId = new Integer(senderId);
        this.queueId = queueId;
        this.retrieveByArrivalTime = retrieveByArrivalTime;
    }

    @Override
    public ReceiveMessageResponse execute(MessagingProtocol protocol) {

        // FIXME .. I don't like if-else
        Optional<Message> message;
        if (senderId == null) {
            message = protocol.receiveMessage(queueId, retrieveByArrivalTime);
        }
        else {
            message = protocol.receiveMessage(senderId, queueId, retrieveByArrivalTime);
        }
        return new ReceiveMessageResponse(message);
    }

    @Override
    public String toString() {
        if (senderId == null) {
            return "(RECEIVE_MESSAGE: " + queueId + ", " + retrieveByArrivalTime + ")";
        }
        else {
            return "(RECEIVE_MESSAGE: " + senderId + ", " + queueId + ", " + retrieveByArrivalTime + ")";
        }
    }
}
