package ch.ethz.inf.asl.middleware;

import ch.ethz.inf.asl.common.request.Request;
import ch.ethz.inf.asl.common.response.Response;
import ch.ethz.inf.asl.logger.MyLogger;
import ch.ethz.inf.asl.middleware.pool.connection.ConnectionPool;
import ch.ethz.inf.asl.middleware.pool.thread.ThreadPool;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

public class Middleware {

    private BlockingQueue<InternalSocket> sockets;
    private MiddlewareRunnable[] middlewareRunnables;
    private ConnectionPool connectionPool;
    private int numberOfThreads;
    private MyLogger logger;
    private int middlewarePortNumber;
    private int databasePortNumber;
    private ServerSocket serverSocket;

    private volatile boolean finished = false;


    public List<Request> getAllRequests() {
        List<Request> list = new LinkedList<>();

        for (MiddlewareRunnable runnable: middlewareRunnables) {
            list.addAll(runnable.getReceivedRequests());
        }

        return list;
    }

    public List<Response> getAllResponses() {
        List<Response> list = new LinkedList<>();

        for (MiddlewareRunnable runnable: middlewareRunnables) {
            list.addAll(runnable.getSentResponses());
        }

        return list;
    }

    // gracefully stop middleware FIXME
    public void stop() {
        for (MiddlewareRunnable runnable: middlewareRunnables) {
            runnable.stop();
        }
        finished = true;
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start(boolean saveEverything) {
        Executor threadPool = new ThreadPool(numberOfThreads);
        middlewareRunnables = new MiddlewareRunnable[numberOfThreads];
        for (int i = 0; i < numberOfThreads; ++i) {
            middlewareRunnables[i] = new MiddlewareRunnable(logger, sockets, connectionPool, saveEverything);
            threadPool.execute(middlewareRunnables[i]);
        }

        try {
            serverSocket = new ServerSocket(middlewarePortNumber);

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


    public Middleware(String[] args) {

        logger = null;
        try {
            logger = new MyLogger("middleware");
        } catch (IOException e) {
            e.printStackTrace();
        }

        String host = args[0];
        String username = args[1];

        String password = args[2];
        if (password.equals("skata")) {
            password = "Foo";
        }
        String databaseName = args[3];
        databasePortNumber = Integer.valueOf(args[4]);
        numberOfThreads = Integer.valueOf(args[5]);
        int connectionPoolSize = Integer.valueOf(args[6]);
        middlewarePortNumber = Integer.valueOf(args[7]);
        sockets = new LinkedBlockingQueue<>();
        connectionPool  = new ConnectionPool(host, databasePortNumber, username, password,
                databaseName, connectionPoolSize, connectionPoolSize);
    }
}
