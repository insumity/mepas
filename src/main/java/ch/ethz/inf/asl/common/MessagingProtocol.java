package ch.ethz.inf.asl.common;

import ch.ethz.inf.asl.utils.Optional;

/**
 * Protocol specification between two entities.
 *
 * @see ch.ethz.inf.asl.client.ClientMessagingProtocolImpl
 * @see ch.ethz.inf.asl.middleware.MiddlewareMessagingProtocolImpl
 */
public interface MessagingProtocol {

    /**
     * Sends given message to the given queue.
     * @param queueId queue where the messages is going to be added
     * @param content message that is sent
     */
    public void sendMessage(int queueId, String content);

    /**
     * Sends given message to the given queue and for the given receiver.
     * @param receiverId the receiver of this message
     * @param queueId queue where the messages is going to be added
     * @param content message that is sent
     */
    public void sendMessage(int receiverId, int queueId, String content);

    /**
     * Receives the message for the user issuing the receival from the specific queue. Or receives the message
     * from this queue intended for any user.
     * @param queueId queue from which the message should be received.
     * @param retrieveByArrivalTime if true and many messages could be returned the one that was inserted
     *                              in the system more far away in the past is returned
     * @return received message, the returned optional could be empty if no message was received
     */
    public Optional<Message> receiveMessage(int queueId, boolean retrieveByArrivalTime);

    /**
     * Receives the message for the user issuing the receival from the specific queue. Or receives the message
     * from this queue intended for any user. The received message was sent by a user with id of {@param senderId}.
     * @param senderId the id of the sender of the message
     * @param queueId queue from where the message is read
     * @param retrieveByArrivalTime if true and many messages could be returned the one that was inserted
     *                              in the system more far away in the past is returned
     * @return received message, the returned optional could be empty if no message was received
     */
    public Optional<Message> receiveMessage(int senderId, int queueId, boolean retrieveByArrivalTime);

    /**
     * Read the message for the user issuing the receival from the specific queue. Or reads the message
     * from this queue intended for any user. A call to this method is not removing the message from the system,
     * so it's possible that the same message can be read multiple times using this method.
     * @param queueId queue from which the message should be received.
     * @param retrieveByArrivalTime if true and many messages could be returned the one that was inserted
     *                              in the system more far away in the past is returned
     * @return read message, the returned optional could be empty if no message was read
     */
    public Optional<Message> readMessage(int queueId, boolean retrieveByArrivalTime);

    /**
     * Query for queues where messages for you are waiting.
     * @return all the queue ids where at least one message for the client issuing
     * the request exists.
     */
    public int[] listQueues();

    /**
     * Informs the other side of the protocol that a new user has just entered the system.
     * @param clientName name of the new client
     * @return the id assigned by the other side of the protocol to this client
     */
    public int sayHello(String clientName);

    /**
     * Informs the other side of the protocol that the user is not going to be issuing any more
     * requests.
     */
    public void sayGoodbye();

    /**
     * Creates a queue in the system.
     * @return the newly created id of the queue
     */
    public int createQueue(String queueName);

    /**
     * Deletes the queue with the given id.
     * @param queueId the id of the queue to be deleted
     */
    public void deleteQueue(int queueId);
}
