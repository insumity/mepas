package ch.ethz.inf.asl;

public interface MessagingProtocol {

    /**
     * Creates a queue in the system.
     * @return the newly created id of the queue
     */
    public int createQueue();

    /**
     * Deletes the queue with the given id.
     * @param queueId the id of the queue to be deleted
     */
    public void deleteQueue(int queueId);

    /**
     * Sends given message to the given queue.
     * @param queueId queue where the messages is going to be added
     * @param msg message that is sent
     */
    public void sendMessage(int queueId, Message msg);

    /**
     * Sends given message to the given queue and for the given receiver.
     * @param receiverId the receiver of this message
     * @param queueId queue where the messages is going to be added
     * @param msg message that is sent
     */
    public void sendMessage(int receiverId, int queueId, Message msg);

    /**
     * Receives the message for the user issuing the receival from the
     * specific queue.
     * @param queueId queue from which the message should be received.
     */
    public void receiveMessage(int queueId);

    /**
     * Reads the topmost message of the queue and removes the message
     * or just returns the topmost message without removing the message.
     * @param queueId queue from where the message is read
     * @param removeMessage if true the message is removed, otherwise it's not
     */
    public void readQueue(int queueId, boolean removeMessage);

    /**
     * Querys/?Receives a message that exists in queue with the given {@param queueId}
     * from the given sender.
     * @param senderId the id of the sender of the message
     * @param queueId queue from where the message is read
     */
    public void queryMessgae(int senderId, int queueId);

    /**
     * Query for queues where messages for you are waiting.
     * @return all the queue ids where at least one message for the client issuing
     * the request exists.
     */
    public int[] queryQueue();
}
