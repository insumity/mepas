package ch.ethz.inf.asl.client;

import ch.ethz.inf.asl.common.Message;
import ch.ethz.inf.asl.exceptions.MessageProtocolException;
import ch.ethz.inf.asl.logger.MyLogger;
import ch.ethz.inf.asl.utils.Optional;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
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

        logger = new MyLogger("Client " + userId);
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
        String hostName = args[0];
        int portNumber = Integer.valueOf(args[1]);

        int totalClients = Integer.valueOf(args[2]);
        int startingId = Integer.valueOf(args[3]);
        // create so many clients int he system

        // create queue with id 1


        Thread[] clients = new Thread[totalClients];

        int ONE_MINUTE = 60;

        for (int i = 0; i < totalClients; ++i) {
            clients[i] =
                    new Thread(new ClientThread(ONE_MINUTE, startingId + i, hostName, portNumber, totalClients));
        }

        // gia na arxisoun oloi mazi ... allios ta new creations argoun FIXME
        for (int i = 0; i < totalClients; ++i) {
            clients[i].start();
        }

        for (int i = 0; i < totalClients; ++i) {
            clients[i].join();
        }

    }

//    public static void main(String[] args) {
//        String hostName = "localhost";
//
//        try {
//            Socket socket = new Socket(hostName, 6789);
//
//            OutputStream os = socket.getOutputStream();
//            InputStream is = socket.getInputStream();
//            ObjectOutputStream oos = new ObjectOutputStream(os);
//            ObjectInputStream ois = new ObjectInputStream(is);
//
//            int requestorId = 1;
//
//            long startingTime = System.currentTimeMillis();
//            while (true) {
//                MessagingProtocol protocol = new ClientMessagingProtocolImpl(requestorId, ois, oos);
//
//                int queueId = protocol.createQueue("coolQueue");
//                protocol.sendMessage(queueId, 1, "hey dude! Everything fine?");
//
//                Optional<Message> receivedMessage = protocol.receiveMessage(queueId, false);
//                if (receivedMessage.isPresent()) {
//                    System.out.println("Message received: " + receivedMessage);
//                }
//                else {
//                    System.out.println("No message was received!");
//                }
//                long endTime = System.currentTimeMillis();
//
//                Thread.sleep(10000);
//                long elapsedSeconds = (endTime - startingTime) / 1000;
//                if (elapsedSeconds >= 20) {
//                    break;
//                }
//            }
//
//            os.close();
//            oos.close();
//            is.close();
//            ois.close();
//            socket.close();;
//
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
    /*
    connect to MW based on configuration
    send messages
    ClientProtocol protocol = new ...Protocol(MW_IP_ADDRESS, ...);

    while(true) {

        protocol.sendMessage(); // actually opens the connection with MW and sends
        a message

        Message msg = protocol.receiveMessage();

    }
     */
}
