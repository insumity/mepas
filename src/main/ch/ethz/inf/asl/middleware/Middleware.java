package ch.ethz.inf.asl.middleware;

import ch.ethz.inf.asl.Message;
import ch.ethz.inf.asl.common.Request;
import ch.ethz.inf.asl.common.Response;
import ch.ethz.inf.asl.utils.Optional;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Middleware {

    static class MiddlewareThread implements Runnable {

        private MWMessagingProtocolImpl protocol;
        private Socket socket;
        public MiddlewareThread(Socket socket, MWMessagingProtocolImpl protocol) {
            this.socket = socket;
            this.protocol = protocol;
        }


        @Override
        public void run() {
            System.err.println("Thread started executing!");
            try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

                // read request ..
                Request request;

                Connection connection = DriverManager.getConnection(
                        "jdbc:postgresql://localhost:5432/tryingstuff", "bandwitch", "");

                while ((request = (Request) ois.readObject()) != null) {


                    protocol = new MWMessagingProtocolImpl(request.getRequestorId(),
                            connection);

                    Response response = null;
                    // based on request ... do stuff
                    if (request.isCreateQueue()) {
                        int queueId = protocol.createQueue(request.getQueueName());
                        response = new Response().createQueue(queueId);
                    }
                    else if (request.isSendMessage()) {
                        protocol.sendMessage(request.getReceiverId(), request.getQueueId(), request.getContent());
                        response = new Response();
                    }
                    else if (request.isReceiveMessage()) {
                        Optional<Message> message = protocol.receiveMessage(request.getQueueId(), true);
                        response = new Response().receiveMessage(message);
                    }

                    System.err.println("(request: " + request + ", response: " + response + ")");
                    out.writeObject(response);
                }

        } catch (IOException e1) {
                e1.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {

        int portNumber = 6789;

        Executor executor = Executors.newFixedThreadPool(2);
        try (
                ServerSocket serverSocket = new ServerSocket(portNumber)) {

            while (true) {
                Socket socket = serverSocket.accept();
                 MiddlewareThread mwthread = new MiddlewareThread(socket, null);
                executor.execute(mwthread);
            }

        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                    + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }
}
