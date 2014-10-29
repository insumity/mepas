package ch.ethz.inf.asl.middleware;

import ch.ethz.inf.asl.common.MessagingProtocol;
import ch.ethz.inf.asl.common.request.Request;
import ch.ethz.inf.asl.common.request.SayGoodbyeRequest;
import ch.ethz.inf.asl.common.response.Response;
import ch.ethz.inf.asl.logger.EmptyLogger;
import ch.ethz.inf.asl.logger.Logger;
import ch.ethz.inf.asl.middleware.pool.connection.ConnectionPool;
import ch.ethz.inf.asl.utils.Helper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import static ch.ethz.inf.asl.utils.Verifier.notNull;

public class MiddlewareRunnable implements Runnable {

    private BlockingQueue<InternalSocket> sockets;
    private ConnectionPool connectionPool;
    private Logger logger;

    private volatile boolean finished = false;

    // to be used for end-to-end testing
    private boolean isEndToEndTest = false;
    private List<Request> receivedRequests;
    private List<Response> sentResponses;

    public MiddlewareRunnable(BlockingQueue<InternalSocket> sockets, ConnectionPool connectionPool,
                              boolean isEndToEndTest) {
        notNull(sockets, "Given sockets cannot be null");
        notNull(connectionPool, "Given connectionPool cannot be null");

        this.sockets = sockets;
        this.connectionPool = connectionPool;

        this.isEndToEndTest = isEndToEndTest;
        this.receivedRequests = new LinkedList<>();
        this.sentResponses = new LinkedList<>();
    }

    public List<Request> getReceivedRequests() {
        return receivedRequests;
    }

    public List<Response> getSentResponses() {
        return sentResponses;
    }

    public void stop() {
        finished = true;
        logger.close();
    }

    private Request getRequestFromFullyReadInternalSocket(InternalSocket internalSocket) {
        byte[] fourBytesLength = ByteBuffer.allocate(4).putInt(internalSocket.getLength()).array();
        byte[] objectData = internalSocket.getObjectData();
        byte[] concatenatedData = Helper.concatenate(fourBytesLength, objectData);

        Request request = null;
        try {
            request = (Request) Helper.deserialize(concatenatedData);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return request;
    }

    @Override
    public void run() {
        if (isEndToEndTest) {
            try {
                this.logger = new EmptyLogger();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                long id = Thread.currentThread().getId();
                String loggersName = String.format("logs/middleware%03d.csv", id);
                this.logger = new Logger(loggersName);
                logger.log("Starting time: " + new Date());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        while (!finished) {

            InternalSocket internalSocket = null;
            try {
                internalSocket = sockets.take();
                long startTime = System.currentTimeMillis(); // FIXME remove
                internalSocket.timesEntered += 1;
                internalSocket.timesToReadARequest += 1;

                logger.log("IN WORKER THREAD\t" + internalSocket.hashCode() + "\t" + (System.currentTimeMillis() - internalSocket.getLastTime()));

                DataOutputStream oos = internalSocket.getOutputStream();
                DataInputStream ois = internalSocket.getInputStream();

                int bytesCanReadWithoutBlocking = ois.available();

                if (bytesCanReadWithoutBlocking == 0) {
                    // cannot read anything right now without blocking
                    internalSocket.setLastTime(System.currentTimeMillis());

                    internalSocket.timesToReadARequest = 0;

                    logger.log("TIME (NOTHING) INSIDE:\t" + (System.currentTimeMillis() - startTime));
                    sockets.put(internalSocket);
                    continue;
                }

                if (!internalSocket.lengthIsKnown()) {
                    byte[] fourBytes = new byte[4];
                    ois.readFully(fourBytes);
                    int length = ByteBuffer.wrap(fourBytes).getInt();
                    internalSocket.setLength(length);
                }

                bytesCanReadWithoutBlocking = ois.available();
                if (bytesCanReadWithoutBlocking == 0) {
                    internalSocket.setLastTime(System.currentTimeMillis());

                    if (internalSocket.lengthIsKnown()) {
                        logger.log("TIME INSIDE:\t" + (System.currentTimeMillis() - startTime));
                    }
                    else {
                        logger.log("TIME (NOTHING) INSIDE:\t" + (System.currentTimeMillis() - startTime));
                    }
                    sockets.put(internalSocket);
                    continue;
                }

                byte[] data = new byte[bytesCanReadWithoutBlocking];
                ois.readFully(data);

                internalSocket.addData(data);
                int currentSize = internalSocket.getBytesRead();
                internalSocket.setBytesRead(currentSize + bytesCanReadWithoutBlocking);

                if (internalSocket.readEverything()) {

                    logger.log("TIMES TO ENTER\t" + internalSocket.timesEntered);
                    logger.log("TIMES TO READ\t" + internalSocket.timesToReadARequest);
                    internalSocket.timesToReadARequest = 0;
                    internalSocket.timesEntered = 0;

                    Request request = getRequestFromFullyReadInternalSocket(internalSocket);

                    Response response;

                    long timeBeforeGettingConnection = System.currentTimeMillis();
                    try (Connection connection = connectionPool.getConnection()) {
                        logger.log(String.format("%d\t%s", System.currentTimeMillis() - timeBeforeGettingConnection, "TIME FOR GETTING CONNECTION"));

                        MessagingProtocol protocol =
                                new MiddlewareMessagingProtocolImpl(logger, request.getRequestorId(), connection);

                        long timeBeforeExecutingRequest = System.currentTimeMillis();
                        response = request.execute(protocol);
                        long timeAfterExecutingRequest = System.currentTimeMillis();
                        logger.log(String.format("%d\t%s\t%s", timeAfterExecutingRequest - timeBeforeExecutingRequest,
                                "TIME FOR EXECUTING REQUEST", request.toString()));
                    }

                    try {
                        oos.write(Helper.serialize(response));
                        oos.flush();
                    }
                    catch (IOException ioe) {
                        // TODO LOG ERROR
                        sockets.remove(internalSocket);
                        continue;
                    }

                    if (isEndToEndTest) {
                        receivedRequests.add(request);
                        sentResponses.add(response);
                    }

                    if (request instanceof SayGoodbyeRequest) {
                        logger.log("TIME (DOING) INSIDE:\t" + (System.currentTimeMillis() - startTime));
                        sockets.remove(internalSocket);
                        continue;
                    }

                    internalSocket.clean();
                }

                internalSocket.setLastTime(System.currentTimeMillis());

                logger.log("TIME (DOING) INSIDE:\t" + (System.currentTimeMillis() - startTime));
                sockets.put(internalSocket);
            } catch (InterruptedException | SQLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("Removed internal socket!");
                sockets.remove(internalSocket);
            }
        }
    }
}