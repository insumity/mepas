package ch.ethz.inf.asl.middleware;

import ch.ethz.inf.asl.common.request.Request;
import ch.ethz.inf.asl.common.response.Response;

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
                    protocol = new MWMessagingProtocolImpl(request.getRequestorId(), connection);
                    System.out.println("Received from client: " + request.getRequestorId()
                        + " the request: " + request);
                    Response response = request.execute(protocol);
                    System.out.println("Got response: " + response + ", to be send to the client!");
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

        int portNumber = Integer.valueOf(args[0]);

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
