package ch.ethz.inf.asl;

public abstract class MessagingProtocol {

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
    public abstract int createQueue();

    /**
     * Deletes the queue with the given id.
     * @param queueId the id of the queue to be deleted
     */
    public abstract void deleteQueue(int queueId);

    /**
     * Sends given message to the given queue.
     * @param queueId queue where the messages is going to be added
     * @param msg message that is sent
     */
    public abstract void sendMessage(int queueId, Message msg);

    /**
     * Sends given message to the given queue and for the given receiver.
     * @param receiverId the receiver of this message
     * @param queueId queue where the messages is going to be added
     * @param msg message that is sent
     */
    public abstract void sendMessage(int receiverId, int queueId, Message msg);

    public abstract Message receiveMessage(int queueId, boolean retrieveByArrivalTime);

    public abstract Message receiveMessage(int senderId, int queueId, boolean retrieveByArrivalTime);

    public abstract Message readMessage(int queueId, boolean retrieveByArrivalTime);

public abstract int[] listQueues();
    /**
     * Receives the message for the user issuing the receival from the
     * specific queue.
     * @param queueId queue from which the message should be received.
     */
//    public abstract void receiveMessage(int queueId);

    /**
     * Reads the topmost message of the queue and removes the message
     * or just returns the topmost message without removing the message.
     * @param queueId queue from where the message is read
     * @param removeMessage if true the message is removed, otherwise it's not
     */
//    public abstract void readQueue(int queueId, boolean removeMessage);

    /**
     * Querys/?Receives a message that exists in queue with the given {@param queueId}
     * from the given sender.
     * @param senderId the id of the sender of the message
     * @param queueId queue from where the message is read
     */
//    public abstract void queryMessage(int senderId, int queueId);

    /**
     * Query for queues where messages for you are waiting.
     * @return all the queue ids where at least one message for the client issuing
     * the request exists.
     */
//    public abstract int[] queryQueue();
}
