package ch.ethz.inf.asl.middleware;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Middleware {


    public static void main(String[] args) {

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(6789);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Socket socket = serverSocket.accept();

            // create a new worker thread
            // this has somehow to be connected with MWMessagingProtocolImpl
            // connection pool
            while (true) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));

                String line = in.readLine();
                System.out.println("before");
                System.out.flush();
                if (line == null) {
                    break;
                }

                System.err.println(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        /*
        Wait for the client to connect
        then create a thread

        when you do
            protocol.createQueue(); actually creates a queue in the DB
            the protocol has the same interface
         */
    }
}
