package ch.ethz.inf.asl.client;

import ch.ethz.inf.asl.utils.ConfigurationReader;
import ch.ethz.inf.asl.common.request.Request;
import ch.ethz.inf.asl.common.response.Response;
import ch.ethz.inf.asl.logger.EmptyLogger;
import ch.ethz.inf.asl.logger.Logger;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Client {

    private List<Request> sentRequests = new LinkedList<>();
    private List<Response> receivedResponses = new LinkedList<>();

    private String hostName;
    private int portNumber;

    // number of clients running in this client instance, different from totalClients which corresponds to the
    // total number of clients in the system
    private int numberOfClients;

    private int startingId;

    private int totalClients;
    private int totalQueues;
    private int messageSize;

    private int runningTimeInSeconds;
    private ClientRunnable[] runnables;
    private Thread[] clients;

    public List<Request> getAllSentRequests() {
        return sentRequests;
    }
    public List<Response> getAllReceivedResponses() {
        return receivedResponses;
    }

    public Client(ConfigurationReader configuration)  {

        this.hostName = configuration.getProperty("middlewareHost");
        this.portNumber = Integer.valueOf(configuration.getProperty("middlewarePortNumber"));

        this.numberOfClients = Integer.valueOf(configuration.getProperty("numberOfClients"));

        this.totalClients = Integer.valueOf(configuration.getProperty("totalClients"));
        this.totalQueues = Integer.valueOf(configuration.getProperty("totalQueues"));
        this.messageSize = Integer.valueOf(configuration.getProperty("messageSize"));

        this.startingId = Integer.valueOf(configuration.getProperty("startingId"));

        this.runningTimeInSeconds = Integer.valueOf(configuration.getProperty("runningTimeInSeconds"));

        runnables = new ClientRunnable[numberOfClients];
        clients = new Thread[numberOfClients];

        this.sentRequests = new LinkedList<>();
        this.receivedResponses = new LinkedList<>();

    }

    public void start(boolean isEndToEndTest) {
        for (int i = 0; i < numberOfClients; ++i) {
            try {
                String loggersName = String.format("logs/client%03d.csv", startingId + i);
                Logger logger;
                if (isEndToEndTest) {
                    logger = new EmptyLogger();
                }
                else {
                    logger = new Logger(loggersName);
                }

                runnables[i] = new ClientRunnable(logger, startingId + i, runningTimeInSeconds, hostName,
                        portNumber, totalClients, totalQueues, messageSize, isEndToEndTest);
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

        if (isEndToEndTest) {
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
