package ch.ethz.inf.asl.middleware;

import ch.ethz.inf.asl.common.ReadConfiguration;
import ch.ethz.inf.asl.common.request.Request;
import ch.ethz.inf.asl.common.response.Response;
import ch.ethz.inf.asl.logger.MyLogger;
import ch.ethz.inf.asl.middleware.pool.connection.ConnectionPool;
import ch.ethz.inf.asl.middleware.pool.thread.ThreadPool;

import java.io.IOException;
import java.io.InputStream;
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
    private MyLogger logger;
    private int middlewarePortNumber;
    private int databasePortNumber;
    private ServerSocket serverSocket;

    private volatile boolean finished = false;
    private volatile boolean started = false;


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

        try {
            serverSocket.close(); // FIXME
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // This code corresponds to a thread that is going to be looking(in a blocking way) at the system input
    // of the middleware and when it reads "STOP" it's going to close the middleware in a gracefully manner
    // by stopping all the middleware runnable threads
    class StoppingMiddleware implements Runnable {
        // gracefully stop middleware


        @Override
        public void run() {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                if (scanner.hasNextLine()) {
                    if (scanner.nextLine().equals("STOP")) {
                        stop();
                        // close the middleware
                        System.exit(0);
                    }
                }
            }
        }
    }

    public void start(boolean saveEverything) {
        Executor threadPool = new ThreadPool(threadPoolSize);
        middlewareRunnables = new MiddlewareRunnable[threadPoolSize];
        for (int i = 0; i < threadPoolSize; ++i) {
            middlewareRunnables[i] = new MiddlewareRunnable(logger, sockets, connectionPool, saveEverything);
            threadPool.execute(middlewareRunnables[i]);
        }

        try {
            serverSocket = new ServerSocket(middlewarePortNumber);
            started = true;

            System.out.println("STARTED");
            // keep an eye on the system input and close the middleware if needed
            new Thread(new StoppingMiddleware()).start();


            while (!finished) {
                sockets.put(new InternalSocket(serverSocket.accept()));
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


    public Middleware(ReadConfiguration configuration) {

        logger = null;
        try {
            logger = new MyLogger("middleware");
        } catch (IOException e) {
            e.printStackTrace();
        }

        String databaseHost = configuration.getProperty("databaseHost");
        databasePortNumber = Integer.valueOf(configuration.getProperty("databasePortNumber"));
        String databaseName = configuration.getProperty("databaseName");
        String databaseUsername = configuration.getProperty("databaseUsername");
        String databasePassword = configuration.getProperty("databasePassword");

        threadPoolSize = Integer.valueOf(configuration.getProperty("threadPoolSize"));
        int connectionPoolSize = Integer.valueOf(configuration.getProperty("connectionPoolSize"));
        String dataSourceName = configuration.getProperty("dataSourceName");
        middlewarePortNumber = Integer.valueOf(configuration.getProperty("middlewarePortNumber"));

        sockets = new LinkedBlockingQueue<>();
        connectionPool  = new ConnectionPool(dataSourceName, databaseHost, databasePortNumber, databaseUsername,
                databasePassword, databaseName, connectionPoolSize, connectionPoolSize);
    }
}
