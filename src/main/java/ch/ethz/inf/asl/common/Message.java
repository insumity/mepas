package ch.ethz.inf.asl.common;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;

import static ch.ethz.inf.asl.common.MessageConstants.MAXIMUM_MESSAGE_LENGTH;
import static ch.ethz.inf.asl.utils.Helper.notNull;
import static ch.ethz.inf.asl.utils.Helper.verifyTrue;

/**
 * Represents a message that is received from the queueing system ... TODO
 */
public class Message implements Serializable {

    private int senderId;
    private Integer receiverId;
    private int queueId;
    private Timestamp arrivalTime;
    private String content;

    public Message(int senderId, int queueId, Timestamp arrivalTime, String content) {
        notNull(arrivalTime, "arrivalTime cannot be null");
        notNull(content, "content cannot be null");
        verifyTrue(content.length() <= MAXIMUM_MESSAGE_LENGTH,
                "content length cannot be greater than: " + MAXIMUM_MESSAGE_LENGTH);

        this.senderId = senderId;
        this.receiverId = null;
        this.queueId = queueId;
        this.arrivalTime = arrivalTime;
        this.content = content;
    }

    public Message(int senderId, int receiverId, int queueId, Timestamp arrivalTime, String content) {
        this(senderId, queueId, arrivalTime, content);
        this.receiverId = receiverId;
    }

    public int getSenderId() {
        return senderId;
    }

    public int getQueueId() {
        return queueId;
    }

    public boolean hasReceiver() {
        return receiverId != null;
    }

    public int getReceiverId() {
        if (!hasReceiver()) {
            throw new NoSuchElementException("There is no receiverId in this message");
        }
        return receiverId;
    }

    public Timestamp getArrivalTime() {
        return arrivalTime;
    }

    public String getContent() {
        return content;
    }


    @Override
    public String toString() {
        return "(senderId: " + senderId + ", receiverId: " + receiverId + ", queueId: " + queueId + ", arrivalTime: " + arrivalTime
                + ", content: \"" + content + "\")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Message) {
            Message other = (Message) obj;

            return this.senderId == other.senderId
                    && Objects.equals(this.receiverId, other.receiverId) // receiverId can be null
                    && this.queueId == other.queueId
                    && this.arrivalTime.equals(other.arrivalTime)
                    && this.content.equals(other.content);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(senderId, receiverId, queueId, arrivalTime, content);
    }
}
