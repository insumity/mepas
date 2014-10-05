package ch.ethz.inf.asl.common.request;

import ch.ethz.inf.asl.common.MessagingProtocol;
import ch.ethz.inf.asl.common.response.ReadMessageResponse;

public class ReadMessageRequest<ReadMeessageResponse> extends Request {

    private int queueId;
    private boolean retrieveByArrivalTime;

    public ReadMessageRequest(int requestorId, int queueId, boolean retrieveByArrivalTime) {
        super(requestorId);
        this.queueId = queueId;
        this.retrieveByArrivalTime = retrieveByArrivalTime;
    }

    @Override
    public ReadMessageResponse execute(MessagingProtocol protocol) {
        return new ReadMessageResponse(protocol.readMessage(queueId, retrieveByArrivalTime));
    }

    @Override
    public String toString() {
        return "(READ_MESSAGE: " + queueId + ", " + retrieveByArrivalTime + ")";
    }

}
