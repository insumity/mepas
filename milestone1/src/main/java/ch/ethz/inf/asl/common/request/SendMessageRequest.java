package ch.ethz.inf.asl.common.request;

import ch.ethz.inf.asl.common.MessagingProtocol;
import ch.ethz.inf.asl.common.response.Response;
import ch.ethz.inf.asl.common.response.SendMessageResponse;
import ch.ethz.inf.asl.exceptions.MessagingProtocolException;

import java.util.Objects;

import static ch.ethz.inf.asl.utils.Verifier.notNull;

public class SendMessageRequest extends Request<SendMessageResponse> {

    private Integer receiverId;
    private int queueId;
    private String content;

    public SendMessageRequest(int requestorId, int queueId, String content) {
        super(requestorId);
        notNull(content, "Given content cannot be null");

        this.receiverId = null;
        this.queueId = queueId;
        this.content = content;
    }

    public SendMessageRequest(int requestorId, int receiverId, int queueId, String content) {
        this(requestorId, queueId, content);
        this.receiverId = receiverId;
    }

    @Override
    public SendMessageResponse execute(MessagingProtocol protocol) {
        notNull(protocol, "Given protocol cannot be null!");

        try {
            if (receiverId == null) {
                protocol.sendMessage(queueId, content);
            }
            else {
                protocol.sendMessage(receiverId, queueId, content);
            }

            return new SendMessageResponse();
        }
        catch (MessagingProtocolException mpe) {
                return Response.createFailedResponse(mpe.getMessage(), SendMessageResponse.class);
        }
    }

    @Override
    public String getName() {
        return "SEND_MESSAGE";
    }

    @Override
    public String toString() {
        return super.toString() + String.format("(%s: [receiverId: %d], [queueId: %d], [content: \"%s\"])",
                getName(), receiverId, queueId, content);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SendMessageRequest) {
            SendMessageRequest other = (SendMessageRequest) obj;
            return super.equals(other) && Objects.equals(this.receiverId, other.receiverId)
                    && Objects.equals(this.queueId, other.queueId) && Objects.equals(this.content, other.content);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), receiverId, queueId, content);
    }
}
