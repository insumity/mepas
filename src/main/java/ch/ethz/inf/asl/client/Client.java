package ch.ethz.inf.asl.client;

import ch.ethz.inf.asl.common.Message;
import ch.ethz.inf.asl.exceptions.MessageProtocolException;
import ch.ethz.inf.asl.logger.MyLogger;
import ch.ethz.inf.asl.utils.Optional;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

class ClientThread implements Runnable {

    // duplicated method FIXME
    private static String stringOf(int length) {
        String a = "";

        for (int i = 0; i < length; ++i) {
            a += "|";
        }
        return a;
    }

    private int userId;
    private String hostName;
    private int portNumber;
    private int totalClients;
    private MyLogger logger;
    private int runningTimeInSeconds;

    public ClientThread(int runningTimeInSeconds, int userId, String hostName, int portNumber, int totalClients) throws IOException {
        this.runningTimeInSeconds = runningTimeInSeconds;
        this.userId = userId;
        this.hostName = hostName;
        this.portNumber = portNumber;
        this.totalClients = totalClients;

        logger = new MyLogger("client" + userId);
    }

    @Override
    public void run() {
        String content = stringOf(200);

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

public class Client {

    public static void main(String[] args) throws IOException, InterruptedException {

        System.err.println("Starting time: " + new Date());
        System.err.println(Arrays.toString(args));
        String hostName = args[0];
        int portNumber = Integer.valueOf(args[1]);

        int totalClients = Integer.valueOf(args[2]);
        int startingId = Integer.valueOf(args[3]);
        int runningTimeInMinutes = Integer.valueOf(args[4]);
        // create so many clients int he system

        // create queue with id 1


        Thread[] clients = new Thread[totalClients];

        // in sceonds FIXME
        int ONE_MINUTE = 60;

        for (int i = 0; i < totalClients; ++i) {
            clients[i] =
                    new Thread(new ClientThread(ONE_MINUTE * runningTimeInMinutes, startingId + i, hostName, portNumber, totalClients));
        }

        // gia na arxisoun oloi mazi ... allios ta new creations argoun FIXME
        for (int i = 0; i < totalClients; ++i) {
            clients[i].start();
        }

        for (int i = 0; i < totalClients; ++i) {
            clients[i].join();
        }

        System.err.println("Ending time: " + new Date());

    }
}
