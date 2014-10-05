package ch.ethz.inf.asl.common.request;

import ch.ethz.inf.asl.common.MessagingProtocol;
import ch.ethz.inf.asl.common.response.SendMesageResponse;

public class SendMessageRequest extends Request {

    private Integer receiverId;
    private int queueId;
    private String content;

    public SendMessageRequest(int requestorId, int queueId, String content) {
        super(requestorId);
        this.receiverId = null;
        this.queueId = queueId;
        this.content = content;
    }

    public SendMessageRequest(int requestorId, int receiverId, int queueId, String content) {
        super(requestorId);
        this.receiverId = new Integer(receiverId);
        this.queueId = queueId;
        this.content = content;
    }

    @Override
    public SendMesageResponse execute(MessagingProtocol protocol) {

        if (receiverId == null) {
            protocol.sendMessage(queueId, content);
        }
        else {
            protocol.sendMessage(receiverId, queueId, content);
        }
        return new SendMesageResponse();
    }

    @Override
    public String toString() {
        if (receiverId == null) {
            return "(SEND_MESSAGE: " + queueId + ", " + content + ")";
        }
        else {
            return "(SEND_MESSAGE: " + receiverId + ", " + queueId + ", " + content + ")";
        }
    }
}
