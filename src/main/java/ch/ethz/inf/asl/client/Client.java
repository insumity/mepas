package ch.ethz.inf.asl.client;

import ch.ethz.inf.asl.common.request.Request;
import ch.ethz.inf.asl.common.response.Response;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class Client {

    private boolean saveEverythng = true;
    private List<Request> sentRequests = new LinkedList<>();
    private List<Response> receiveResponses = new LinkedList<>();

    public List<Request> getAllSentRequets() {
        return sentRequests;
    }

    public List<Response> getAllReceivedResponses() {
        return receiveResponses;
    }

    public Client(String[] args, boolean saveEverythng)  {

        System.err.println("Starting time: " + new Date());
        System.err.println(Arrays.toString(args));
        String hostName = args[0];
        int portNumber = Integer.valueOf(args[1]);

        int totalClients = Integer.valueOf(args[2]);
        int startingId = Integer.valueOf(args[3]);
        int runningTimeInSeconds = Integer.valueOf(args[4]);

        ClientRunnable[] runnables = new ClientRunnable[totalClients];
        Thread[] clients = new Thread[totalClients];

        this.saveEverythng = saveEverythng;
        this.sentRequests = new LinkedList<>();
        this.receiveResponses = new LinkedList<>();

        for (int i = 0; i < totalClients; ++i) {
            try {
                runnables[i] = new ClientRunnable(runningTimeInSeconds, startingId + i, hostName, portNumber, totalClients, saveEverythng);
                clients[i] = new Thread(runnables[i]);
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

        for (ClientRunnable runnable: runnables) {
            sentRequests.addAll(runnable.getSentRequets());
            receiveResponses.addAll(runnable.getReceivedResponse());
        }

        System.err.println("Ending time: " + new Date());

    }
}
