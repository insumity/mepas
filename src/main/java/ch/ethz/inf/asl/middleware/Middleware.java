package ch.ethz.inf.asl.middleware;

import ch.ethz.inf.asl.common.MessagingProtocol;
import ch.ethz.inf.asl.common.request.Request;
import ch.ethz.inf.asl.common.response.Response;
import org.postgresql.ds.PGPoolingDataSource;

import javax.activation.DataSource;
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

        private PGPoolingDataSource source;
        private Socket socket;
        public MiddlewareThread(Socket socket, PGPoolingDataSource source) {
            this.socket = socket;
            this.source = source;
        }


        @Override
        public void run() {
            System.err.println("Thread started executing!");
            try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

                // read request ..
                Request request;

                while ((request = (Request) ois.readObject()) != null) {
                    Connection conn = source.getConnection();
                    MessagingProtocol protocol = new MWMessagingProtocolImpl(request.getRequestorId(), conn);
                    System.out.println("Received from client: " + request.getRequestorId()
                        + " the request: " + request);
                    Response response = request.execute(protocol);
                    System.out.println("Got response: " + response + ", to be send to the client!");
                    out.writeObject(response);
                    conn.close();
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

    public static void main(String[] args) throws IOException, SQLException {

        int portNumber = Integer.valueOf(args[0]);

        // initialize connection pool (from: http://jdbc.postgresql.org/documentation/head/ds-ds.html)
        PGPoolingDataSource source = new PGPoolingDataSource();
        source.setDataSourceName("connection pool");
        source.setServerName("localhost");
        source.setDatabaseName("tryingstuff");
        source.setUser("bandwitch");
        source.setPassword("");
        source.setInitialConnections(3);
        source.setMaxConnections(3);
        source.initialize();

        Connection one;
        Connection two;
        Connection three;

        one = source.getConnection();
        two = source.getConnection();
        three = source.getConnection();

        one.close();
        two.close();
        three.close();
        one = source.getConnection();
        two = source.getConnection();
        three = source.getConnection();



        Executor executor = Executors.newFixedThreadPool(2);
        try (
                ServerSocket serverSocket = new ServerSocket(portNumber)) {

            while (true) {
                Socket socket = serverSocket.accept();
                    MiddlewareThread mwthread = new MiddlewareThread(socket, source);
                executor.execute(mwthread);
            }

        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                    + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }
}
