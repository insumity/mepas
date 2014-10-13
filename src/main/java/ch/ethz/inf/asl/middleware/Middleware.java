package ch.ethz.inf.asl.middleware;

import ch.ethz.inf.asl.common.MessagingProtocol;
import ch.ethz.inf.asl.common.request.Request;
import ch.ethz.inf.asl.common.response.Response;
import ch.ethz.inf.asl.logger.MyLogger;
import org.postgresql.ds.PGPoolingDataSource;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class Middleware {

    public static PGPoolingDataSource initiateConnectionPool(String host, String username,
                                                             String password, String db, int size) throws SQLException {

        // initialize connection pool (from: http://jdbc.postgresql.org/documentation/head/ds-ds.html)
        PGPoolingDataSource source = new PGPoolingDataSource();
        source.setDataSourceName("connection pool");
        source.setServerName(host);
        source.setDatabaseName(db);
        source.setUser(username);
        source.setPassword(password);
        source.setInitialConnections(size);
        source.setMaxConnections(size);
        source.initialize();
        return source;
    }

    static class InternalSocket {

        private int bytesRead;
        private int lengthOfUpcomingObject;
        private Socket socket;
        private boolean lengthIsKnown;
        private List<byte[]> whatWasRead;
        private DataOutputStream oos;
        private BufferedInputStream ois;

        public InternalSocket(Socket socket) throws IOException {
            this.socket = socket;
            this.lengthIsKnown = false;
            whatWasRead = new LinkedList<>();

            // THIS IS MADDNESS
            oos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream(), 100));

            // those strems need to be used for the lifetime of the socket
            oos.flush();
            ois = new BufferedInputStream(socket.getInputStream(), 100);
        }

        public DataOutputStream getOutputStream() {
            return oos;
        }

        public BufferedInputStream getInputStream() {
            return ois;
        }

        public boolean lengthIsKnown() {
            return lengthIsKnown;
        }

        public Socket getSocket() {
            return socket;
        }

        public int getBytesRead() {
            return bytesRead;
        }

        public void setBytesRead(int bytesRead) {
            this.bytesRead = bytesRead;
        }

        public int getLengthOfUpcomingObject() {
            return lengthOfUpcomingObject;
        }

        public void setLengthOfUpcomingObject(int lengthOfUpcomingObject) {
            this.lengthOfUpcomingObject = lengthOfUpcomingObject;
            this.lengthIsKnown = true;
        }

        public boolean readEverything() {
            return lengthIsKnown && (bytesRead == lengthOfUpcomingObject);
        }

        public void addData(byte[] dataRead) {
            whatWasRead.add(dataRead);
        }

        public byte[] getObjectData() {
            byte[] objectData = new byte[lengthOfUpcomingObject];

            int j = 0;
            for (byte[] ar: whatWasRead) {
                for (int i = 0; i < ar.length; ++i) {
                    objectData[j] = ar[i];
                    ++j;
                }
            }

            return objectData;
        }

        public void clean() {
            this.lengthIsKnown = false;
            this.bytesRead = 0;
            this.lengthOfUpcomingObject = 0;
            this.whatWasRead.clear();
        }
    }


    static class WorkerThread implements Runnable {

        private BlockingQueue<InternalSocket> sockets;
        private PGPoolingDataSource source;
        private MyLogger logger;

        public WorkerThread(MyLogger logger, BlockingQueue<InternalSocket> sockets, PGPoolingDataSource source) {
            this.sockets = sockets;
            this.source = source;
            this.logger = logger;
        }

        public Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            ObjectInputStream is = new ObjectInputStream(in);
            return is.readObject();
        }

        // concat function taken from http://stackoverflow.com/questions/80476/how-to-concatenate-two-arrays-in-java
        public byte[] concat(byte[] first, byte[] second) {
            byte[] result = Arrays.copyOf(first, first.length + second.length);
            System.arraycopy(second, 0, result, first.length, second.length);
            return result;
        }

        private byte[] objectToByteArray(Object object) throws IOException {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(object);

            byte[] objectData = bos.toByteArray();
            byte[] lengthOfObject = ByteBuffer.allocate(4).putInt(objectData.length).array();
            byte[] toSend = concat(lengthOfObject, objectData);

            return toSend;
        }

        @Override
        public void run() {

            String s = "This is cool!";
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutput out = new ObjectOutputStream(bos);
                out.writeObject(s);
                byte[] data = bos.toByteArray();
                String x = (String) deserialize(data);
                System.err.println(x);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }


            while (true) {
                try {
                    long id = Thread.currentThread().getId();
                    logger.synchronizedLog(id, "size: " + sockets.size());
                    logger.synchronizedLog(id, "I'm here in the beginning of the while loop");
                    InternalSocket internalSocket = sockets.take();
                    logger.synchronizedLog(id, "I got a socket!");

                    // remove closed connections FIXME ...
                    if (internalSocket.getSocket().isClosed()) {
                        logger.synchronizedLog(id, " A socket was removed because it got closed!");
                        sockets.remove(internalSocket);
                        continue;
                    }

                    DataOutputStream oos = internalSocket.getOutputStream();
                    BufferedInputStream ois = internalSocket.getInputStream();

                    int bytesCanReadWithoutBlocking = ois.available();
                    logger.synchronizedLog(id, "Bytes I can read without blocking: " + bytesCanReadWithoutBlocking);
                    if (internalSocket.lengthIsKnown()) {
                        logger.synchronizedLog(id, "the length of the socket is known!");
                        byte[] data = new byte[bytesCanReadWithoutBlocking];
                        logger.synchronizedLog(id, "Trying to read data");
                        int bytesActuallyRead = ois.read(data);
                        logger.synchronizedLog(id, "read data!");
                        internalSocket.addData(data);
                        int currentSize = internalSocket.getBytesRead();
                        internalSocket.setBytesRead(currentSize + bytesActuallyRead);
                        logger.synchronizedLog(id, "bytes read: " + internalSocket.getBytesRead());
                        logger.synchronizedLog(id, "length to be read: " + internalSocket.getLengthOfUpcomingObject());
                        if (internalSocket.readEverything()) {

                            logger.synchronizedLog(id, "I read everything");
                            byte[] objectData = internalSocket.getObjectData();

                            Request request = (Request) deserialize(objectData);
                            logger.synchronizedLog(id, "I deserialized the request");

                            Connection conn;
                            synchronized (source) {
                                conn = source.getConnection();
                                logger.synchronizedLog(id, "The connection I got: " + conn.hashCode());
                            }

                            logger.synchronizedLog(id, "I got the connection");
                            MessagingProtocol protocol = new MWMessagingProtocolImpl(logger, request.getRequestorId(), conn);
                            logger.synchronizedLog(id, "created the protocol");

                            System.out.println("Received from client: " + request.getRequestorId()
                                    + " the request: " + request);


                            Response response = request.execute(protocol);
                            logger.synchronizedLog(id, "executed the request!");
                            conn.close();
                            logger.synchronizedLog(id, "closed the connection");
                            System.out.println("Got response: " + response + ", to be send to the client!");

                            logger.synchronizedLog(id, "writing the response");
                            oos.write(objectToByteArray(response));
                            logger.synchronizedLog(id, "response was written");

                            // clean internal socket
                            internalSocket.clean();
                        }
                    }
                    else {
                        if (bytesCanReadWithoutBlocking >= 4) {
                            // is this correct? TODO readint could return less bytes
                            byte[] fourBytes = new byte[4];
                            logger.synchronizedLog(id, "going to read length");
                            int bytesRead = ois.read(fourBytes); // TODO .. should be 4
                            logger.synchronizedLog(id, "length read");

                            int length = ByteBuffer.wrap(fourBytes).getInt();
                            internalSocket.setLengthOfUpcomingObject(length);
                        }
                    }

                    // put socket back to it's world
                    // TODO ... clients that leave .. should leave also from the sockets list
                    logger.synchronizedLog(id, "putting socket back with" +
                    sockets.size());
                    sockets.put(internalSocket);
                    logger.synchronizedLog(id, "the socket is back with current" +
                            "size "+ sockets.size());

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws IOException, SQLException {

        MyLogger logger = new MyLogger("middleware");

        System.err.println(Arrays.toString(args));
        String host = args[0];
        String username = args[1];
        String password = args[2] + "1$2$3$"; // TODO WTF
        String dbName = args[3];

        int portNumber = Integer.valueOf(args[4]);
        int numberOfThreads = Integer.valueOf(args[5]);
        int connectionPoolSize = Integer.valueOf(args[6]);

        BlockingQueue<InternalSocket> sockets = new LinkedBlockingQueue<>();

        PGPoolingDataSource source = initiateConnectionPool(host, username, password, dbName, connectionPoolSize);
        Executor executor = Executors.newFixedThreadPool(numberOfThreads);
        for (int i = 0; i < numberOfThreads; ++i) {
            executor.execute(new WorkerThread(logger, sockets, source));
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
