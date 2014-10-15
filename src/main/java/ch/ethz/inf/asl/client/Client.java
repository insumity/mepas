package ch.ethz.inf.asl.client;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

public class Client {

    public Client(String[] args)  {

        System.err.println("Starting time: " + new Date());
        System.err.println(Arrays.toString(args));
        String hostName = args[0];
        int portNumber = Integer.valueOf(args[1]);

        int totalClients = Integer.valueOf(args[2]);
        int startingId = Integer.valueOf(args[3]);
        int runningTimeInMinutes = Integer.valueOf(args[4]);

        Thread[] clients = new Thread[totalClients];

        int ONE_MINUTE_IN_SECONDS = 60;

        for (int i = 0; i < totalClients; ++i) {
            try {
                clients[i] =
                        new Thread(new ClientThread(runningTimeInMinutes, startingId + i, hostName, portNumber, totalClients));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // start all the clients
        for (int i = 0; i < totalClients; ++i) {
            clients[i].start();
        }

        // wait until all clients have finished
        for (int i = 0; i < totalClients; ++i) {
            try {
                clients[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.err.println("Ending time: " + new Date());

    }
}
