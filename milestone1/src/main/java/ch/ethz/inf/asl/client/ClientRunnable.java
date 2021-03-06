package ch.ethz.inf.asl.client;

import ch.ethz.inf.asl.common.Message;
import ch.ethz.inf.asl.common.request.Request;
import ch.ethz.inf.asl.common.response.Response;
import ch.ethz.inf.asl.exceptions.MessagingProtocolException;
import ch.ethz.inf.asl.logger.Logger;
import ch.ethz.inf.asl.utils.Optional;

import java.io.IOException;
import java.net.Socket;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static ch.ethz.inf.asl.utils.Verifier.*;

/**
 * This class is executed in a thread and corresponds to one client of the system.
 */
public class ClientRunnable implements Runnable {

    private String hostName;
    private int portNumber;

    private int totalClients;
    private int totalQueues;

    // size of messages in number of characters
    private int messageSize;

    private Logger logger;
    private int runningTimeInSeconds;

    private int userId;

    private ClientMessagingProtocolImpl protocol;

    private boolean isEndToEndTest;

    public List<Request> getSentRequests() {
        return protocol.getSentRequests();
    }

    public List<Response> getReceivedResponses() {
        return protocol.getReceivedResponses();
    }

    /**
     * Initialized an object given the parameters.
     * @param logger to be used for logging the activities of the client
     * @param userId the id of the specific client
     * @param runningTimeInSeconds how much time the client is going to be running
     * @param hostName middleware's host address
     * @param portNumber port of where the middleware
     * @param totalClients total clients in the system
     * @param totalQueues total number of queues in the system
     * @param messageSize size of the messages to be sent counted in characters
     * @param isEndToEndTest true if this is an end-to-end test, false otherwise
     */
    public ClientRunnable(Logger logger, int userId, int runningTimeInSeconds, String hostName, int portNumber, int totalClients,
                          int totalQueues, int messageSize, boolean isEndToEndTest) {
        notNull(logger, "Given logger cannot be null!");
        verifyTrue(runningTimeInSeconds > 0, "Given runningTimeInSeconds cannot be negative or 0!");
        hasText(hostName , "Given hostName cannot be empty or null!");
        verifyTrue(portNumber > 0, "Given portNumber cannot be 0 or negative!");
        verifyTrue(totalClients > 0, "Given totalClients cannot be 0 or negative!");
        verifyTrue(totalQueues > 0, "Given totalQueues cannot be 0 or negative!");
        verifyTrue(messageSize > 0, "Given messageSize cannot be 0 or negative!");

        this.runningTimeInSeconds = runningTimeInSeconds;
        this.userId = userId;
        this.hostName = hostName;
        this.portNumber = portNumber;

        this.totalClients = totalClients;
        this.totalQueues = totalQueues;
        this.messageSize = messageSize;

        this.logger = logger;

        this.isEndToEndTest = isEndToEndTest;
    }

    // returns a random receiverId in the range [1, totalClients] that is not this.userId
    private int getRandomReceiverId() {
        Random r = new Random();
        int receiverId = r.nextInt(totalClients) + 1;
        while (receiverId == userId) {
            receiverId = r.nextInt(totalClients) + 1;
        }
        return receiverId;
    }

    // returns a random queueId in the range [1, totalQueues]
    private int getRandomQueueId() {
        return new Random().nextInt(totalQueues) + 1;
    }

    // taken from: http://stackoverflow.com/questions/41107/how-to-generate-a-random-alpha-numeric-string
    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    static Random rnd = new Random();

    // creates a StringBuilder of given length containing random data
    private StringBuilder randomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));

        return sb;
    }

    // sends a message using the underlying message protocol to a random receiver and random queue
    private void sendMessage(int messageSize) {
        long startTime = System.currentTimeMillis();

        int receiverId = getRandomReceiverId();
        int queueId = getRandomQueueId();

        String content = randomString(messageSize).toString();

        protocol.sendMessage(receiverId, queueId, content);

        // do not log the whole content of the message
        int maxLength = (content.length() > 5)? 5: content.length();
        logger.log((System.currentTimeMillis() - startTime) + "\tSEND_MESSAGE\t" + "(" + queueId + ", "
                + receiverId + ", " + content.substring(0, maxLength) + ")");
    }

    // lists all the queues where a message for the user is waiting and issues a receive for one message on all
    // those queues
    private void listAndReceiveMessage() {
        long startTime = System.currentTimeMillis();

        int[] queues = protocol.listQueues();
        logger.log((System.currentTimeMillis() - startTime) + "\tLIST_QUEUES");

        for (int queueId: queues) {
            startTime = System.currentTimeMillis();

            Optional<Message> message = protocol.receiveMessage(queueId, false);

            if (!message.isPresent()) {
                throw new AssertionError("There should have been a message received!");
            }

            Message actualMessage = message.get();
            String content = actualMessage.getContent();
            // only log 5 characters max from the content so the logs aren't that big
            int maxLength = (content.length() > 5)? 5: content.length();

            logger.log((System.currentTimeMillis() - startTime) + "\tRECEIVE_MESSAGE\t" +
                    "(" + actualMessage.getQueueId() + ", " + actualMessage.getSenderId()
                    + ", " + content.substring(0, maxLength) + ")");
        }

    }

    @Override
    public void run() {

        Socket socket = null;
        try {
            socket = new Socket(hostName, portNumber);
            protocol = new ClientMessagingProtocolImpl(socket, userId, isEndToEndTest);
        } catch (IOException e) {
            e.printStackTrace();
        }


        long startingTime = System.currentTimeMillis();
        logger.log("Starting time: " + new Date().toString());

        // sending and list-receiving is done alternately
        boolean toSend = true;

        while (true) {
            long elapseTimeInSeconds = (System.currentTimeMillis() - startingTime) / 1000;

            // the client run his given amount of time
            if (elapseTimeInSeconds >= runningTimeInSeconds) {
                if (socket != null) {
                    System.out.println("Client socket got closed!");
                    protocol.sayGoodbye();
                }
                break;
            }

            try {
                if (toSend) {
                    sendMessage(messageSize);
                } else {
                    listAndReceiveMessage();
                }
                toSend = !toSend;

            } catch (MessagingProtocolException e) {
                e.printStackTrace();
                break;
            }
        }

        System.out.println("CLOSED");
        logger.close();
    }

}