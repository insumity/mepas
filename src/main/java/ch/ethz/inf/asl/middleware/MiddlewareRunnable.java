package ch.ethz.inf.asl.middleware;

import ch.ethz.inf.asl.common.MessagingProtocol;
import ch.ethz.inf.asl.common.request.SayGoodbyeRequest;
import ch.ethz.inf.asl.common.request.Request;
import ch.ethz.inf.asl.common.response.Response;
import ch.ethz.inf.asl.logger.Logger;
import ch.ethz.inf.asl.middleware.pool.connection.ConnectionPool;
import ch.ethz.inf.asl.utils.Helper;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.SQLException;
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
    private boolean saveEverything = false;
    private List<Request> receivedRequests;
    private List<Response> sentResponses;


    public MiddlewareRunnable(Logger logger, BlockingQueue<InternalSocket> sockets, ConnectionPool connectionPool,
                              boolean saveEverything) {
        notNull(logger, "Given logger cannot be null");
        notNull(sockets, "Given sockets cannot be null");
        notNull(connectionPool, "Given connectionPool cannot be null");

        this.sockets = sockets;
        this.connectionPool = connectionPool;
        this.logger = logger;

        this.saveEverything = saveEverything;
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
    }

    private Request getRequestFromFullyReadInternalSocket(InternalSocket internalSocket) {
        byte[] fourBytesLength = ByteBuffer.allocate(4).putInt(internalSocket.getLength()).array();
        byte[] objectData = internalSocket.getObjectData();
        byte[] concatenatedData = Helper.concatenate(fourBytesLength, objectData);

        Request request = null;
        try {
            request = (Request) Helper.deserialize(concatenatedData);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return request;
    }

    @Override
    public void run() {

        while (!finished) {

            InternalSocket internalSocket = null;

            try {
                internalSocket = sockets.take();

                DataOutputStream oos = internalSocket.getOutputStream();
                BufferedInputStream ois = internalSocket.getInputStream();

                int bytesCanReadWithoutBlocking = ois.available();

                if (bytesCanReadWithoutBlocking == 0) {
                    // cannot read anything right now without blocking
                    sockets.put(internalSocket);
                    continue;
                }

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
                        Request request = getRequestFromFullyReadInternalSocket(internalSocket);

                        Response response;
                        try (Connection connection = connectionPool.getConnection()) {
                            MessagingProtocol protocol =
                                    new MiddlewareMessagingProtocolImpl(logger, request.getRequestorId(), connection);

                            response = request.execute(protocol);
                        }

                        try {
                            oos.write(Helper.serialize(response));
                        }
                        catch (IOException ioe) {
                            // TODO LOG ERROR
                            sockets.remove(internalSocket);
                            continue;
                        }

                        if (saveEverything) {
                            receivedRequests.add(request);
                            sentResponses.add(response);
                        }

                        if (request instanceof SayGoodbyeRequest) {
                            sockets.remove(internalSocket);
                            continue;
                        }

                        internalSocket.clean();
                    }
                }
                else {
                    // is this correct? TODO read int could return less bytes
                    byte[] fourBytes = new byte[4];
                    int bytesRead = ois.read(fourBytes); // TODO .. should be 4
                    if (bytesRead == -1) {
                        sockets.remove(internalSocket);
                        continue;
                    }

                    int length = ByteBuffer.wrap(fourBytes).getInt();
                    internalSocket.setLength(length);
                }

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