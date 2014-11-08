package ch.ethz.inf.asl.middleware;

import ch.ethz.inf.asl.common.request.Request;
import ch.ethz.inf.asl.common.response.Response;
import ch.ethz.inf.asl.middleware.pool.connection.ConnectionPool;
import ch.ethz.inf.asl.middleware.pool.thread.ThreadPool;
import ch.ethz.inf.asl.utils.ConfigurationReader;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

public class Middleware {

    private BlockingQueue<InternalSocket> sockets;
    private MiddlewareRunnable[] middlewareRunnables;
    private ConnectionPool connectionPool;
    private int threadPoolSize;
    private int middlewarePortNumber;
    private ServerSocket serverSocket;

    private volatile boolean finished = false;
    private volatile boolean started = false;

    private List<InternalSocket> internalSocketsCreated;

    public List<Request> getAllRequests() {
        List<Request> list = new LinkedList<>();

        for (MiddlewareRunnable runnable: middlewareRunnables) {
            list.addAll(runnable.getReceivedRequests());
        }

        return list;
    }

    public boolean hasStarted() {
        return started;
    }

    public List<Response> getAllResponses() {
        List<Response> list = new LinkedList<>();

        for (MiddlewareRunnable runnable: middlewareRunnables) {
            list.addAll(runnable.getSentResponses());
        }

        return list;
    }

    public void stop() {
        for (MiddlewareRunnable runnable : middlewareRunnables) {
            runnable.stop();
        }

        // close the pool
        connectionPool.close();

        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // shut down the middleware
        System.exit(0);
    }

    // This code corresponds to a thread that is going to be looking(in a blocking way) at the system input
    // of the middleware and when it reads "STOP" it's going to close the middleware in a gracefully manner
    // by stopping all the middleware runnable threads
    private class MiddlewareStopper implements Runnable {
        // gracefully stop middleware

        @Override
        public void run() {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                if (scanner.hasNextLine() && scanner.nextLine().equals("STOP")) {
                    stop();
                }
            }
        }
    }

    public void start(boolean isEndToEndTest) {
        Executor threadPool = new ThreadPool(threadPoolSize);
        middlewareRunnables = new MiddlewareRunnable[threadPoolSize];
        for (int i = 0; i < threadPoolSize; ++i) {
            middlewareRunnables[i] = new MiddlewareRunnable(sockets, connectionPool, isEndToEndTest);
            threadPool.execute(middlewareRunnables[i]);
        }

        try {
            serverSocket = new ServerSocket(middlewarePortNumber);
            started = true;

            // keep an eye on the system input and stop the middleware if needed
            new Thread(new MiddlewareStopper()).start();

            System.out.println("STARTED");
            while (!finished) {
                InternalSocket internalSocket = new InternalSocket(serverSocket.accept());
                internalSocketsCreated.add(internalSocket);

                sockets.put(internalSocket);
            }
        } catch (SocketException e) {
            System.err.println("The serverSocket was probably abruptly closed! Did somebody stop it?");
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public Middleware(ConfigurationReader configuration) {

        String databaseHost = configuration.getProperty("databaseHost");
        int databasePortNumber = Integer.valueOf(configuration.getProperty("databasePortNumber"));
        String databaseName = configuration.getProperty("databaseName");
        String databaseUsername = configuration.getProperty("databaseUsername");
        String databasePassword = configuration.getProperty("databasePassword");

        int connectionPoolSize = Integer.valueOf(configuration.getProperty("connectionPoolSize"));

        this.threadPoolSize = Integer.valueOf(configuration.getProperty("threadPoolSize"));
        this.middlewarePortNumber = Integer.valueOf(configuration.getProperty("middlewarePortNumber"));

        sockets = new LinkedBlockingQueue<>();
        connectionPool  = new ConnectionPool(databaseHost, databasePortNumber, databaseUsername,
                databasePassword, databaseName, connectionPoolSize);

        internalSocketsCreated = new LinkedList<>();
    }
}
