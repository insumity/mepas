package ch.ethz.inf.asl.common;

import java.io.Serializable;

import static ch.ethz.inf.asl.common.MessageType.*;

public class Request implements Serializable {

    private int requestorId;
    private MessageType type;

    private String queueName; // CREATE_QUEUE

    private int queueId; // DELETE_QUEUE, SEND_MESSAGE, RECEIVE_MESSAGE

    private String content; // SEND_MESSAGE
    private int receiverId; // SEND_MESSAGE

    private boolean retrieveByArrivalTime; // SEND_MESSAGE

    public Request(int requestorId) {
        this.requestorId = requestorId;
    }

    public Request createQueue(String queueName) {
        this.type = CREATE_QUEUE;
        this.queueName = queueName;
        return this;
    }

    public Request deleteQueue(int queueId) {
        this.type = DELETE_QUEUE;
        this.queueId = queueId;
        return this;
    }

    public Request sendMessage(int queueId, String content) {
        this.type = SEND_MESSAGE;
        this.queueId = queueId;
        this.content = content;
        return this;
    }

    public Request sendMessage(int receiverId, int queueId, String content) {
        this.type = SEND_MESSAGE;
        this.receiverId = receiverId;
        this.queueId = queueId;
        this.content = content;
        return this;
    }

    public Request receiveMessage(int queueId, boolean retrieveByArrivalTime) {
        this.type = RECEIVE_MESSAGE;
        this.queueId = queueId;
        this.retrieveByArrivalTime = retrieveByArrivalTime;
        return this;
    }

    public int getRequestorId() {
        return requestorId;
    }

    public MessageType getType() {
        return type;
    }

    public String getQueueName() {
        return queueName;
    }

    public int getQueueId() {
        return queueId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public String getContent() {
        return content;
    }

    public boolean isRetrieveByArrivalTime() {
        return retrieveByArrivalTime;
    }

    @Override
    public String toString() {
        return "(requestorId: " + requestorId + ", type: " +  type + ")";
    }
}
