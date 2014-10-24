package ch.ethz.inf.asl.client;

import ch.ethz.inf.asl.common.Message;
import ch.ethz.inf.asl.common.request.Request;
import ch.ethz.inf.asl.common.response.Response;
import ch.ethz.inf.asl.exceptions.MessagingProtocolException;
import ch.ethz.inf.asl.logger.Logger;
import ch.ethz.inf.asl.utils.Optional;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Random;

import static ch.ethz.inf.asl.utils.Verifier.hasText;

public class ClientRunnable implements Runnable {

    //    private int userId;
    private String hostName;
    private int portNumber;

    private int totalClients;

    private Logger logger;
    private int runningTimeInSeconds;

    private int userId;

    private ClientMessagingProtocolImpl protocol;

    private boolean saveEverything;

    public List<Request> getSentRequests() {
        return protocol.getSentRequests();
    }

    public List<Response> getReceivedResponses() {
        return protocol.getReceivedResponses();
    }

    public ClientRunnable(Logger logger, int userId, int runningTimeInSeconds, String hostName, int portNumber, int totalClients,
                          boolean saveEverything) throws IOException {
        hasText(hostName , "hostName cannot be empty or null");

        this.runningTimeInSeconds = runningTimeInSeconds;
        this.userId = userId;
        this.hostName = hostName;
        this.portNumber = portNumber;

        this.totalClients = totalClients;
        this.saveEverything = saveEverything;
        this.logger = logger;
    }

    @Override
    public void run() {

        Socket kkSocket = null;
        try {
            kkSocket = new Socket(hostName, portNumber);
        } catch (IOException e) {
            e.printStackTrace();
        }

        protocol = null;
        try {
            protocol = new ClientMessagingProtocolImpl(kkSocket, userId, saveEverything);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int lastMessageCounter = 0;
        boolean send = true;
        long startingTime = System.currentTimeMillis();
        while (true) {

            long currentTime = System.currentTimeMillis();
            long elapseTimeInSeconds = (currentTime - startingTime) / 1000;
            if (elapseTimeInSeconds >= runningTimeInSeconds) {
                if (kkSocket != null) {
                    System.out.println("Client socket got closed!");
                    protocol.sayGoodbye();
                }

                break;
            }
            try {
                if (send) {
//                    Random r = new Random();
//                    int sendToUserId = r.nextInt(totalClients) + 1;
//                    while (sendToUserId == userId) {
//                        sendToUserId = r.nextInt(totalClients) + 1;
//                    }

                    long startTime = System.currentTimeMillis();
                    Random r = new Random();
                    long randomLong = r.nextLong();

                    protocol.sendMessage(1, String.valueOf(lastMessageCounter));
                    long responseTime = System.currentTimeMillis() - startTime;
                    logger.log(System.currentTimeMillis() - startingTime, responseTime + "\tSEND_MESSAGE\t" + String.valueOf(lastMessageCounter));

                    send = !send;
                } else {
                    long startTime = System.currentTimeMillis();
                    Optional<Message> message = protocol.receiveMessage(1, true);
                    long responseTime = System.currentTimeMillis() - startTime;

                    if (message.isPresent()) {
                        logger.log(System.currentTimeMillis() - startingTime, responseTime + "\tRECEIVE_MESSAGE\t" + message.isPresent()
                                + "\t" + message.get().hashCode() + "\t" + message.get());
                    }
                    else {
                        logger.log(System.currentTimeMillis() - startingTime, responseTime + "\tRECEIVE_MESSAGE\t" + message.isPresent());
                    }
                    lastMessageCounter = lastMessageCounter + 1;

                    send = !send;
                }
            } catch (MessagingProtocolException e) {
                e.printStackTrace();
                break;
            }
        }

        System.out.println("CLOSED");

        logger.close();
    }
}