package ch.ethz.inf.asl.middleware;

import ch.ethz.inf.asl.common.Request;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
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
            try (
                  ObjectOutputStream out =
                          new ObjectOutputStream(socket.getOutputStream());
                  ObjectInputStream ois = new ObjectInputStream(
                          socket.getInputStream());
            ) {

                Request request;

                while ((request = (Request) ois.readObject()) != null) {
                    // based on request ... do stuff
                    if (request.isCreateQueue()) {
                        int queueId = protocol.createQueue(request.getQueueName());
                    }
                    else if (request.)


                    System.err.println("I read your data: " + request);
                    out.writeObject("it's me the server again!");
                }

        } catch (IOException e1) {
                e1.printStackTrace();
            } catch (ClassNotFoundException e) {
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
                MiddlewareThread mwthread = new MiddlewareThread(socket);
                executor.execute(mwthread);
            }

        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                    + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }
//
//    public static void main(String[] args) {
//
//        ServerSocket serverSocket = null;
//        try {
//            serverSocket = new ServerSocket(6789);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        try (Socket socket = serverSocket.accept()) {
//
//            long startTime = System.currentTimeMillis();
//            while (true) {
//                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
//
//                String read = br.readLine();
//                System.out.println(">> " + read);
//
//                bw.write("Hola amigo! It's the server");
//
//                long endTime = System.currentTimeMillis();
//                int elapsedSeconds = (int) ((endTime - startTime) / 1000);
//                if (elapsedSeconds > 20) {
//                    break;
//                }
//
//                br.close();
//                bw.close();
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        /*
//        Wait for the client to connect
//        then create a thread
//
//        when you do
//            protocol.createQueue(); actually creates a queue in the DB
//            the protocol has the same interface
//         */
//    }
}
