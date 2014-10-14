package ch.ethz.inf.asl.client;

import ch.ethz.inf.asl.common.Message;
import ch.ethz.inf.asl.exceptions.MessageProtocolException;
import ch.ethz.inf.asl.logger.MyLogger;
import ch.ethz.inf.asl.utils.Optional;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Random;

import static ch.ethz.inf.asl.utils.Helper.hasText;

public class ClientThread implements Runnable {

    private int userId;
    private String hostName;
    private int portNumber;
    private int totalClients;
    private MyLogger logger;
    private int runningTimeInSeconds;

    public ClientThread(int runningTimeInSeconds, int userId, String hostName, int portNumber, int totalClients) throws IOException {
        hasText(hostName , "hostName cannot be empty or null");

        this.runningTimeInSeconds = runningTimeInSeconds;
        System.out.println(runningTimeInSeconds + "seconds");
        this.userId = userId;
        this.hostName = hostName;
        this.portNumber = portNumber;
        this.totalClients = totalClients;

        String loggersName = String.format("client%03d", userId);
        logger = new MyLogger(loggersName);
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

        ClientMessagingProtocolImpl protocol = null;
        try {
            protocol = new ClientMessagingProtocolImpl(userId, kkSocket);
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
                    try {
                        System.out.println("Client socket got closed!");
                        protocol.sayGoodbye();
                        kkSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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

        logger.close();
    }
}