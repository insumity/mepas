package ch.ethz.inf.asl.client;

import ch.ethz.inf.asl.Message;
import ch.ethz.inf.asl.MessagingProtocol;
import ch.ethz.inf.asl.common.MessageType;
import ch.ethz.inf.asl.common.Request;
import ch.ethz.inf.asl.common.Response;
import ch.ethz.inf.asl.utils.Optional;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

    public static void main(String[] args) {

        int userId = Integer.valueOf(args[0]);
        int otherUserId;
        if (userId == 1) {
            otherUserId = 2;
        }
        else {
            otherUserId = 1;
        }

        String hostName = "localhost";
        int portNumber = 6789;
        String content = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        System.out.println(content.length());

        try (
        Socket kkSocket = new Socket(hostName, portNumber);

        ObjectOutputStream objectOutputStream = new ObjectOutputStream(kkSocket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(
                kkSocket.getInputStream());
        ) {
            Response fromServer;

            // initiate conversation with server by sending the request
            Request request = new Request(userId).createQueue("mepas");

            // send message
            objectOutputStream.writeObject(request);

            boolean send = true;
            while ((fromServer = (Response) in.readObject()) != null) {
                System.out.println("Server response: " + fromServer);

                Thread.sleep(2000);
                if (send) {
                    request = new Request(userId).sendMessage(otherUserId, 1, content);
                    objectOutputStream.writeObject(request);
                    send = !send;
                }
                else {
                    request = new Request(userId).receiveMessage(1, true);
                    objectOutputStream.writeObject(request);
                    send = !send;
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
        } catch (ClassNotFoundException e) {
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
