package ch.ethz.inf.asl.middleware;

import ch.ethz.inf.asl.logger.MyLogger;
import ch.ethz.inf.asl.middleware.pool.connection.ConnectionPool;
import ch.ethz.inf.asl.middleware.pool.thread.ThreadPool;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

public class Middleware {


    public Middleware(String[] args) {

        MyLogger logger = null;
        try {
            logger = new MyLogger("middleware");
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.err.println(Arrays.toString(args));
        String host = args[0];
        String username = args[1];
        String password = args[2];
        String dbName = args[3];

        int portNumber = Integer.valueOf(args[4]);
        int numberOfThreads = Integer.valueOf(args[5]);
        int connectionPoolSize = Integer.valueOf(args[6]);

        BlockingQueue<InternalSocket> sockets = new LinkedBlockingQueue<>();

        ConnectionPool connectionPool  = new ConnectionPool(host, portNumber, username, password, dbName, connectionPoolSize, connectionPoolSize);

        Executor threadPool = new ThreadPool(numberOfThreads);
        for (int i = 0; i < numberOfThreads; ++i) {
            threadPool.execute(new MiddlewareThread(logger, sockets, connectionPool));
        }

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            while (true) {
                sockets.put(new InternalSocket(serverSocket.accept()));
            }
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                    + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
