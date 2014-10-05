package ch.ethz.inf.asl.middleware;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;

public class MiddlewareWorkerThread implements Runnable {

    private Socket socket;
    private Connection connectionToDatabase;

    public MiddlewareWorkerThread(Socket socket, Connection connectionToDatabase) {
        this.socket = socket;
        this.connectionToDatabase = connectionToDatabase;
    }

    @Override
    public void run() {
        while (true) {
            try {
                // read request


                // based on request execute method from
                int userId = 5;
                MWMessagingProtocolImpl messagingProtocol = new MWMessagingProtocolImpl(userId, connectionToDatabase);

//                    int queueId = messagingProtocol.createQueue();
                    // blah blah blah

                // create response
                // send back reepsonse

                PrintWriter out =
                        new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
