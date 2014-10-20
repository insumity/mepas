package ch.ethz.inf.asl.client;

import ch.ethz.inf.asl.common.ReadConfiguration;
import ch.ethz.inf.asl.common.request.Request;
import ch.ethz.inf.asl.common.response.Response;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Client {

    private List<Request> sentRequests = new LinkedList<>();
    private List<Response> receivedResponses = new LinkedList<>();

    private String hostName;
    private int portNumber;

    private int numberOfClients;
    private int startingId;
    private int totalClients;

    private int runningTimeInSeconds;
    private ClientRunnable[] runnables;
    private Thread[] clients;

    public List<Request> getAllSentRequets() {
        return sentRequests;
    }

    public List<Response> getAllReceivedResponses() {
        return receivedResponses;
    }

    public Client(ReadConfiguration configuration)  {

        this.hostName = configuration.getProperty("middlewareHost");
        this.portNumber = Integer.valueOf(configuration.getProperty("middlewarePortNumber"));

        this.numberOfClients = Integer.valueOf(configuration.getProperty("numberOfClients"));
        this.totalClients = Integer.valueOf(configuration.getProperty("totalClients"));
        this.startingId = Integer.valueOf(configuration.getProperty("startingId"));

        this.runningTimeInSeconds = Integer.valueOf(configuration.getProperty("runningTimeInSeconds"));

        runnables = new ClientRunnable[numberOfClients];
        clients = new Thread[numberOfClients];

        this.sentRequests = new LinkedList<>();
        this.receivedResponses = new LinkedList<>();

    }

    public void start(boolean saveEverything) {
        for (int i = 0; i < numberOfClients; ++i) {
            try {
                runnables[i] = new ClientRunnable(startingId + i, runningTimeInSeconds, hostName, portNumber, totalClients, saveEverything);
                clients[i] = new Thread(runnables[i]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // start all the clients
        for (int i = 0; i < numberOfClients; ++i) {
            clients[i].start();
        }

        // wait until all clients have finished
        for (int i = 0; i < numberOfClients; ++i) {
            try {
                clients[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (saveEverything) {
            for (ClientRunnable runnable: runnables) {
                sentRequests.addAll(
                        runnable.getSentRequests());
                receivedResponses.addAll(
                        runnable.getReceivedResponses());
            }
        }

        System.out.println("FINISHED");
    }
}
