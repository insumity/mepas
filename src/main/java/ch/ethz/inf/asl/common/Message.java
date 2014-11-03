package ch.ethz.inf.asl.common;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;

import static ch.ethz.inf.asl.common.MessageConstants.MAXIMUM_MESSAGE_LENGTH;
import static ch.ethz.inf.asl.utils.Verifier.notNull;
import static ch.ethz.inf.asl.utils.Verifier.verifyTrue;

/**
 * This class represents a message that was received by the messaging system.
 */
public class Message implements Serializable {

    private int senderId;
    private Integer receiverId;
    private int queueId;
    private Timestamp arrivalTime;
    private String content;

    /**
     * Initialized a new created Message object with the given parameters. This message
     * contains no receiver.
     * @param senderId id of the sender of this message
     * @param queueId id of where this message was sent to
     * @param arrivalTime time the message arrived in the messaging system
     * @param content content of the message
     */
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

    /**
     * Initialized a new created Message object with the given parameters.
     * @param senderId id of the sender of this message
     * @param receiverId id of the receiver of this message
     * @param queueId id of where this message was sent to
     * @param arrivalTime time the message arrived in the messaging system
     * @param content content of the message
     */
    public Message(int senderId, int receiverId, int queueId, Timestamp arrivalTime, String content) {
        this(senderId, queueId, arrivalTime, content);
        this.receiverId = receiverId;
    }

    /**
     * Returns sender id of the message, i.e. the user id who sent this message.
     * @return sender id
     */
    public int getSenderId() {
        return senderId;
    }

    /**
     * Returns id of the queue where this message was sent to.
     * @return queue id
     */
    public int getQueueId() {
        return queueId;
    }

    /**
     * Checks whether this message has a receiver or not.
     * @return true if this message is sent to a specific receiver, false otherwise
     */
    public boolean hasReceiver() {
        return receiverId != null;
    }

    /**
     * Returns the receiver id of this message, i.e. the user id who this message
     * is intended to.
     * @return receiver id of this message
     * @throws java.util.NoSuchElementException in case this message has no receiver, check {@link #hasReceiver()}
     */
    public int getReceiverId() {
        if (!hasReceiver()) {
            throw new NoSuchElementException("There is no receiverId in this message");
        }
        return receiverId;
    }

    /**
     * Returns the arrival time of the message, i.e. when the message entered the messaging system.
     * @return arrival time of message
     */
    public Timestamp getArrivalTime() {
        return arrivalTime;
    }

    /**
     * Gets the content of the message.
     * @return content of message
     */
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
