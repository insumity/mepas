package ch.ethz.inf.asl.client;

import ch.ethz.inf.asl.common.Message;
import ch.ethz.inf.asl.common.request.Request;
import ch.ethz.inf.asl.common.response.Response;
import ch.ethz.inf.asl.exceptions.MessagingProtocolException;
import ch.ethz.inf.asl.logger.Logger;
import ch.ethz.inf.asl.utils.Optional;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static ch.ethz.inf.asl.utils.Verifier.hasText;
import static ch.ethz.inf.asl.utils.Verifier.notNull;
import static ch.ethz.inf.asl.utils.Verifier.verifyTrue;

public class ClientRunnable implements Runnable {

    private String hostName;
    private int portNumber;

    private int totalClients;
    private int totalQueues;

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

    public ClientRunnable(Logger logger, int userId, int runningTimeInSeconds, String hostName, int portNumber, int totalClients,
                          int totalQueues , boolean isEndToEndTest) throws IOException {
        notNull(logger, "Given logger cannot be null!");
        verifyTrue(runningTimeInSeconds > 0, "Given runningTimeInSeconds cannot be negative or 0!");
        hasText(hostName , "Given hostName cannot be empty or null!");
        verifyTrue(portNumber > 0, "Given portNumber cannot be 0 or negative!");
        verifyTrue(totalClients > 0, "Given totalClients cannot be 0 or negative!");
        verifyTrue(totalQueues > 0, "Given totalQueues cannot be 0 or negative!");

        this.runningTimeInSeconds = runningTimeInSeconds;
        this.userId = userId;
        this.hostName = hostName;
        this.portNumber = portNumber;

        this.totalClients = totalClients;
        this.totalQueues = totalQueues;

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

    // sends a message using the underlying message protocol to a random receiver and random queue
    private void sendMessage(int messageCounter) {
        long startTime = System.currentTimeMillis();

        int receiverId = getRandomReceiverId();
        int queueId = getRandomQueueId();

        protocol.sendMessage(receiverId, queueId, String.valueOf(messageCounter));
        logger.log((System.currentTimeMillis() - startTime) + "\tSEND_MESSAGE\t" + "(" + queueId + ", "
                + receiverId + ", " + String.valueOf(messageCounter) + ")");
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
            logger.log((System.currentTimeMillis() - startTime) + "\tRECEIVE_MESSAGE\t" +
                    "(" + actualMessage.getQueueId() + ", " + actualMessage.getSenderId()
                    + ", " + actualMessage.getContent() + ")");
        }

    }

    @Override
    public void run() {

        Socket socket = null;
        try {
            socket = new Socket(hostName, portNumber);
            protocol = new ClientMessagingProtocolImpl(logger, socket, userId, isEndToEndTest);
        } catch (IOException e) {
            e.printStackTrace();
        }


        long startingTime = System.currentTimeMillis();
        logger.log("Starting time: " + new Date().toString());


        // sending and list-receiving is done alternately
        boolean toSend = true;
        int messageCounter = 1;

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
                    sendMessage(messageCounter);
                    messageCounter++;
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