package ch.ethz.inf.asl.client;

import ch.ethz.inf.asl.common.Message;
import ch.ethz.inf.asl.exceptions.MessageProtocolException;
import ch.ethz.inf.asl.utils.Optional;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

    // duplicated method FIXME
    private static String stringOf(int length) {
        String a = "";

        for (int i = 0; i < length; ++i) {
            a += "|";
        }
        return a;
    }

    public static void main(String[] args) {

        int userId = Integer.valueOf(args[0]);
        int otherUserId = Integer.valueOf(args[1]);
        String hostName = args[2];
        int portNumber = Integer.valueOf(args[3]);
        String content = stringOf(200);

        try (Socket kkSocket = new Socket(hostName, portNumber))
         {
            ClientMessagingProtocolImpl protocol = new ClientMessagingProtocolImpl(userId, kkSocket);

            int result = protocol.createQueue("mepasCool");
            System.out.println("Queue created with queue id: " + result);

            boolean send = true;
            while (true) {

                Thread.sleep(2000);
                try {
                    if (send) {
                        protocol.sendMessage(otherUserId, 1, content);
                        System.err.println("Send message");
                        send = !send;
                    } else {
                        Optional<Message> message = protocol.receiveMessage(1, true);
                        if (message.isPresent()) {
                            System.err.println("Received message: " + message.get());
                        }
                        else {
                            System.err.println("Received message: NONE");
                        }
                        send = !send;
                    }
                } catch (MessageProtocolException e) {
                    e.printStackTrace();
                    break;
                }
            }

        }
         catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                    hostName);
            System.exit(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
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
