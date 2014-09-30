package ch.ethz.inf.asl.common;

import ch.ethz.inf.asl.Message;
import ch.ethz.inf.asl.utils.Optional;

import java.io.Serializable;

public class Response implements Serializable {

    private int queueId; // CREATE_QUEUE
    private Optional<Message> message; // RECEIVE_MESSAGE

    public Response createQueue(int queueId) {
        this.queueId = queueId;
        return this;
    }

    public Response receiveMessage(Optional<Message> message) {
        this.message = message;
        return this;
    }

    public int getQueueId() {
        return queueId;
    }

    public Optional<Message> getMessage() {
        return message;
    }
}
