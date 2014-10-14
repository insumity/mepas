package ch.ethz.inf.asl.middleware;

import ch.ethz.inf.asl.common.MessagingProtocol;
import ch.ethz.inf.asl.common.request.GoodbyeRequest;
import ch.ethz.inf.asl.common.request.Request;
import ch.ethz.inf.asl.common.response.Response;
import ch.ethz.inf.asl.logger.MyLogger;
import ch.ethz.inf.asl.middleware.pool.connection.ConnectionPool;
import ch.ethz.inf.asl.utils.Helper;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;

import static ch.ethz.inf.asl.utils.Helper.notNull;

public class MiddlewareThread implements Runnable {

    private BlockingQueue<InternalSocket> sockets;
    private ConnectionPool connectionPool;
    private MyLogger logger;

    public MiddlewareThread(MyLogger logger, BlockingQueue<InternalSocket> sockets, ConnectionPool connectionPool) {
        notNull(logger, "Given logger cannot be null");
        notNull(sockets, "Given sockets cannot be null");
        notNull(connectionPool, "Given connectionPool cannot be null");

        this.sockets = sockets;
        this.connectionPool = connectionPool;
        this.logger = logger;
    }


    @Override
    public void run() {

        while (true) {

            InternalSocket internalSocket = null;

            try {
                long id = Thread.currentThread().getId();
                internalSocket = sockets.take();

                DataOutputStream oos = internalSocket.getOutputStream();
                BufferedInputStream ois = internalSocket.getInputStream();

                int bytesCanReadWithoutBlocking = ois.available();
                if (internalSocket.lengthIsKnown()) {
                    byte[] data = new byte[bytesCanReadWithoutBlocking];
                    int bytesActuallyRead = ois.read(data);

                    // no more data
                    if (bytesActuallyRead == -1) {
                        sockets.remove(internalSocket);
                        continue;
                    }

                    internalSocket.addData(data);
                    int currentSize = internalSocket.getBytesRead();
                    internalSocket.setBytesRead(currentSize + bytesActuallyRead);
                    if (internalSocket.readEverything()) {

                        byte[] objectData = internalSocket.getObjectData();

                        Request request = (Request) Helper.deserialize(objectData);

                        Response response = null;
                        try (Connection connection = connectionPool.getConnection()) {
                            MessagingProtocol protocol =
                                    new MiddlewareMessagingProtocolImpl(logger, request.getRequestorId(), connection);

                            System.out.println("Received from client: " + request.getRequestorId() + " the request: " + request);

                            if (request instanceof GoodbyeRequest) {
                                System.err.println("I removed the client!");
                                sockets.remove(internalSocket);
                                    continue;
                            }

                            response = request.execute(protocol);
                        }

                        System.out.println("Got response: " + response + ", to be send to the client!");

                        try {
                            oos.write(Helper.serialize(response));
                        }
                        catch (IOException ioe) {
                            sockets.remove(internalSocket);
                            continue;
                        }

                        // clean internal socket
                        internalSocket.clean();
                    }
                }
                else {
                    if (bytesCanReadWithoutBlocking >= 4) {
                        // is this correct? TODO readint could return less bytes
                        byte[] fourBytes = new byte[4];
                        int bytesRead = ois.read(fourBytes); // TODO .. should be 4
                        if (bytesRead == -1) {
                            sockets.remove(internalSocket);
                            continue;
                        }

                        int length = ByteBuffer.wrap(fourBytes).getInt();
                        internalSocket.setLength(length);
                    }
                    else {
                        // YOU cannot know when your peer closed the socket without blocking
//                        int x = ois.read(new byte[1]);
                        System.out.println("I was here: " + bytesCanReadWithoutBlocking + ", ");
                    }
                }

                // put socket back to it's world
                // TODO ... clients that leave .. should leave also from the sockets list
                sockets.put(internalSocket);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("Removed internal socket!");
                sockets.remove(internalSocket);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}