package ch.ethz.inf.asl.client;

import ch.ethz.inf.asl.common.Message;
import ch.ethz.inf.asl.common.request.Request;
import ch.ethz.inf.asl.common.response.Response;
import ch.ethz.inf.asl.exceptions.MessageProtocolException;
import ch.ethz.inf.asl.logger.MyLogger;
import ch.ethz.inf.asl.utils.Optional;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static ch.ethz.inf.asl.utils.Verifier.hasText;

public class ClientRunnable implements Runnable {

//    private int userId;
    private String hostName;
    private int portNumber;
    private int totalClients;
    private MyLogger logger;
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

    public ClientRunnable(int userId, int runningTimeInSeconds, String hostName, int portNumber, int totalClients,
                          boolean saveEverything) throws IOException {
        hasText(hostName , "hostName cannot be empty or null");

        this.runningTimeInSeconds = runningTimeInSeconds;
        this.userId = userId;
        this.hostName = hostName;
        this.portNumber = portNumber;
        this.totalClients = totalClients;

        String loggersName = String.format("logs/client%03d.csv", userId);
        logger = new MyLogger(loggersName);

        this.saveEverything = saveEverything;
    }

    @Override
    public void run() {
        String content = "some content";

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
                    Random r = new Random();
                    int sendToUserId = r.nextInt(totalClients) + 1;
                    while (sendToUserId == userId) {
                        sendToUserId = r.nextInt(totalClients) + 1;
                    }

                    long startTime = System.currentTimeMillis();
                    protocol.sendMessage(sendToUserId, 1, content);
                    logger.log(System.currentTimeMillis() - startTime, "SEND_MESSAGE\t" + sendToUserId + "\t");

                    send = !send;
                } else {
                    long startTime = System.currentTimeMillis();
                    Optional<Message> message = protocol.receiveMessage(1, true);
                    logger.log(System.currentTimeMillis() - startTime, "RECEIVE_MESSAGE\t" + message.isPresent());
                    send = !send;
                }
            } catch (MessageProtocolException e) {
                e.printStackTrace();
                break;
            }
        }

        System.out.println("CLOSED");

        logger.close();
    }
}