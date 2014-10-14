package ch.ethz.inf.asl.common;

import ch.ethz.inf.asl.utils.Optional;

public abstract class MessagingProtocol {

    // FIXME fix java docs
    private int requestorId;

    /**
     *
     * @param requestorId the id of the user issuing the request
     */
    public void MessagingProtocol(int requestorId) {
        this.requestorId = requestorId;
    }

    /**
     * Creates a queue in the system.
     * @return the newly created id of the queue
     */
    public abstract int createQueue(String queueName);

    /**
     * Deletes the queue with the given id.
     * @param queueId the id of the queue to be deleted
     */
    public abstract void deleteQueue(int queueId);

    /**
     * Sends given message to the given queue.
     * @param queueId queue where the messages is going to be added
     * @param content message that is sent
     */
    public abstract void sendMessage(int queueId, String content);

    /**
     * Sends given message to the given queue and for the given receiver.
     * @param receiverId the receiver of this message
     * @param queueId queue where the messages is going to be added
     * @param content message that is sent
     */
    public abstract void sendMessage(int receiverId, int queueId, String content);

    /**
     * Receives the message for the user issuing the receival from the
     * specific queue.
     * @param queueId queue from which the message should be received.
     */
    public abstract Optional<Message> receiveMessage(int queueId, boolean retrieveByArrivalTime);

    /**
     * Querys/?Receives a message that exists in queue with the given {@param queueId}
     * from the given sender.
     * @param senderId the id of the sender of the message
     * @param queueId queue from where the message is read
     */
    public abstract Optional<Message> receiveMessage(int senderId, int queueId, boolean retrieveByArrivalTime);

    public abstract Optional<Message> readMessage(int queueId, boolean retrieveByArrivalTime);

    /**
     * Query for queues where messages for you are waiting.
     * @return all the queue ids where at least one message for the client issuing
     * the request exists.
     */
    public abstract int[] listQueues();


    /* why this messages is need is explained in the "OnTheBlockingSolution" */
    public abstract void sayGoodbye();

}
