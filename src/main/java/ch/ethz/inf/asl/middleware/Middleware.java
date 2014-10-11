package ch.ethz.inf.asl.middleware;

import ch.ethz.inf.asl.common.MessagingProtocol;
import ch.ethz.inf.asl.common.request.Request;
import ch.ethz.inf.asl.common.response.Response;
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

    static class MiddlewareThread implements Runnable {

        private PGPoolingDataSource source;
        private Socket socket;
        public MiddlewareThread(Socket socket, PGPoolingDataSource source) {
            this.socket = socket;
            this.source = source;
        }


        @Override
        public void run() {
            System.err.println("Thread started executing!");
            try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

                // read request ..
                Request request;

                while ((request = (Request) ois.readObject()) != null) {
                    Connection conn = source.getConnection();
                    MessagingProtocol protocol = new MWMessagingProtocolImpl(request.getRequestorId(), conn);
                    System.out.println("Received from client: " + request.getRequestorId()
                        + " the request: " + request);
                    Response response = request.execute(protocol);
                    System.out.println("Got response: " + response + ", to be send to the client!");
                    out.writeObject(response);
                    conn.close();
                }

        } catch (IOException e1) {
                e1.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static PGPoolingDataSource initiateConnectionPool() throws SQLException {
        // initialize connection pool (from: http://jdbc.postgresql.org/documentation/head/ds-ds.html)
        PGPoolingDataSource source = new PGPoolingDataSource();
        source.setDataSourceName("connection pool");
        source.setServerName("localhost");
        source.setDatabaseName("tryingstuff");
        source.setUser("bandwitch");
        source.setPassword("");
        source.setInitialConnections(10);
        source.setMaxConnections(10);
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

        public WorkerThread(BlockingQueue<InternalSocket> sockets, PGPoolingDataSource source) {
            this.sockets = sockets;
            this.source = source;
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
                    InternalSocket internalSocket = sockets.take();

                    DataOutputStream oos = internalSocket.getOutputStream();
                    BufferedInputStream ois = internalSocket.getInputStream();

                    int bytesCanReadWithoutBlocking = ois.available();

                    if (internalSocket.lengthIsKnown()) {
                        byte[] data = new byte[bytesCanReadWithoutBlocking];
                        int bytesActuallyRead = ois.read(data);
                        internalSocket.addData(data);

                        int currentSize = internalSocket.getBytesRead();
                        internalSocket.setBytesRead(currentSize + bytesActuallyRead);

                        if (internalSocket.readEverything()) {

                            byte[] objectData = internalSocket.getObjectData();

                            Request request = (Request) deserialize(objectData);

                            Connection conn = source.getConnection();
                            MessagingProtocol protocol = new MWMessagingProtocolImpl(request.getRequestorId(), conn);

                            System.out.println("Received from client: " + request.getRequestorId()
                                    + " the request: " + request);


                            Response response = request.execute(protocol);
                            conn.close();
                            System.out.println("Got response: " + response + ", to be send to the client!");

                            oos.write(objectToByteArray(response));

                            // clean internal socket
                            internalSocket.clean();
                        }
                    }
                    else {
                        if (bytesCanReadWithoutBlocking >= 4) {
                            // is this correct? TODO readint could return less bytes
                            byte[] fourBytes = new byte[4];
                            int bytesRead = ois.read(fourBytes);
                            int length = ByteBuffer.wrap(fourBytes).getInt();
                            internalSocket.setLengthOfUpcomingObject(length);
                        }
                    }

                    // put socket back to it's world
                    // TODO ... clients that leave .. should leave also from the sockets list
                    sockets.put(internalSocket);

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


        int portNumber = Integer.valueOf(args[0]);

        BlockingQueue<InternalSocket> sockets = new LinkedBlockingQueue<>();

        PGPoolingDataSource source = initiateConnectionPool();
        int NUMBER_OF_THREADS = 10;
        Executor executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        for (int i = 0; i < NUMBER_OF_THREADS; ++i) {
            executor.execute(new WorkerThread(sockets, source));
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
