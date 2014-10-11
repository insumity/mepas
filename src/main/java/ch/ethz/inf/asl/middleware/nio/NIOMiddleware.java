package ch.ethz.inf.asl.middleware.nio;

import ch.ethz.inf.asl.common.MessagingProtocol;
import ch.ethz.inf.asl.common.request.Request;
import ch.ethz.inf.asl.common.response.Response;
import ch.ethz.inf.asl.middleware.MWMessagingProtocolImpl;
import org.postgresql.ds.PGPoolingDataSource;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


class SerializedObject {
    private int length;
    private List<byte[]> data;
    private boolean isEmpty = true;

    public SerializedObject() {
        data = new LinkedList<>();
        length = 0;
        isEmpty = true;
    }

    public void setLength(int length) {
        this.length = length;
        isEmpty = false;
    }

    public int getLength() {
        return length;
    }

    public boolean isEmpty() {
        return isEmpty;
    }
    public void appendData(byte[] data) {
        this.data.add(data);
    }

    public byte[] getObjectByteArray() {
        byte[] allData = new byte[size()];

        int i = 0;
        for (byte[] ar: data) {
            for (int j = 0; j < ar.length; ++j) {
                allData[i] = ar[j];
                ++i;
            }
        }
        return allData;
    }

    public int size() {
        int size = 0;
        for (byte[] ar: data) {
            size += ar.length;
        }
        return size;
    }

    public void clear() {
        data = new LinkedList<>();
        length = 0;
        isEmpty = true;
    }
}


public class NIOMiddleware {

    // A pre-allocated buffer for encrypting data
    private static final ByteBuffer buffer = ByteBuffer.allocate(10);

    // read data from socketchannel
    private static byte[] processInput(SocketChannel sc) throws IOException, ClassNotFoundException {
        buffer.clear();
        sc.read( buffer );
        buffer.flip();

        // If no data, close the connection
        if (buffer.limit()==0) {
            return new byte[0];
        }

        int data = buffer.limit();
        byte[] dataBytes = new byte[data];
        buffer.get(dataBytes);
        return dataBytes;
    }

    /* gets a connection for the given database */
    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        Class.forName("org.postgresql.Driver");
        return DriverManager.getConnection("jdbc:postgresql://localhost:5432/tryingstuff", "postgres", "");
    }

    // concat function taken from http://stackoverflow.com/questions/80476/how-to-concatenate-two-arrays-in-java
    public static byte[] concat(byte[] first, byte[] second) {
        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    private static byte[] objectToByteArray(Object object) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(bos);
        out.writeObject(object);

        byte[] objectData = bos.toByteArray();
        byte[] lengthOfObject = ByteBuffer.allocate(4).putInt(objectData.length).array();
        byte[] toSend = concat(lengthOfObject, objectData);

        return toSend;
    }

    public static void main(String[] args) throws IOException, SQLException {

        int port = Integer.valueOf(args[0]);

        // create a ServerSocketChannel
        ServerSocketChannel ssc = ServerSocketChannel.open();

        // Set it to non-blocking, so we can use select
        ssc.configureBlocking( false );

        // Get the Socket connected to this channel, and bind it
        // to the listening port
        ServerSocket ss = ssc.socket();
        InetSocketAddress isa = new InetSocketAddress( port );
        ss.bind( isa );

        // Create a new Selector for selecting
        Selector selector = Selector.open();

        // Register the ServerSocketChannel, so we can
        // listen for incoming connections
        ssc.register( selector, SelectionKey.OP_ACCEPT );
        System.out.println("Listening on port " + port );

        while (true) {
            // See if we've had any activity -- either
            // an incoming connection, or incoming data on an
            // existing connection
            System.err.println("Before select");
            int num = selector.select();
            System.err.println("After select!");

            // If we don't have any activity, loop around and wait
            // again
            if (num == 0) {
                continue;
            }

            // Get the keys corresponding to the activity
            // that has been detected, and process them
            // one by one
            Set<SelectionKey> keys = selector.selectedKeys();
            for (SelectionKey key : keys) {
                // Get a key representing one of bits of I/O
                // activity (key)

                // TODO
                if (key.isValid()) {
                    continue; //
                }

                // What kind of activity is it?
                if (key.isAcceptable()) {

                    System.out.println( "acc" );
                    // It's an incoming connection. Register this socket with the Selector
                    // so we can listen for input on it

                    Socket s = ss.accept();
                    System.out.println( "Got connection from "+s );

                    // Make sure to make it non-blocking, so we can use a selector on it.
                    SocketChannel sc = s.getChannel();
                    sc.configureBlocking( false );

                    // Register it with the selector, for reading
                    sc.register( selector, SelectionKey.OP_READ );
                } else if (key.isReadable()) {

                    // new object
                    if (key.attachment() == null ) {
                        key.attach(new SerializedObject());
                    }
                    SocketChannel sc = null;

                    try {

                        // It's incoming data on a connection, so
                        // process it
                        sc = (SocketChannel)key.channel();

                        System.err.println(sc.hashCode());
                        byte[] readData = processInput(sc);
                        SerializedObject object = (SerializedObject) key.attachment();

                        if (object.isEmpty()) {
                            assert (readData.length >= 4);
                            byte[] numd = new byte[4];
                            for (int j = 0; j < 4; ++j) {
                                numd[j] = readData[j];
                            }

                            int length = ByteBuffer.wrap(numd).getInt();
                            object.setLength(length);

                            byte[] restOfData = new byte[readData.length - 4];
                            for (int i = 0; i < readData.length - 4; ++i) {
                                restOfData[i] = readData[i + 4];
                            }
                            object.appendData(restOfData);
                        }
                        else {
                            object.appendData(readData);
                        }

                        System.err.println("EISAI TRELA!!!!");

                        // object fully read
                        if (object.size() == object.getLength()) {
                            ByteArrayInputStream bari = new ByteArrayInputStream(object.getObjectByteArray());
                            ObjectInputStream ois = new ObjectInputStream(bari);
                            Request x = (Request) ois.readObject();

                            object.clear(); // delete object for next read

                            System.err.println("object is: " + x);

                            Connection connection = getConnection();
                            Response response = x.execute(new MWMessagingProtocolImpl(x.getRequestorId(), connection));
                            connection.close();

                            byte[] responseData = objectToByteArray(response);
                            ByteBuffer buffer = ByteBuffer.wrap(responseData);

                            // needs to verify that responseData bytes were written
                            int bytesWritten = sc.write(buffer);
                            System.err.println("BytesWritten: " + bytesWritten);
                        }


                        System.out.println("Key attachment for this channel: " + key.attachment());

                        // If the connection is dead, then remove it from the selector and close it
                        if (readData.length == 0) {
                            System.err.println("closed conneciton!");
                            key.cancel();

                            Socket s = null;
                            try {
                                s = sc.socket();
                                s.close();
                            } catch( IOException ie ) {
                                System.err.println( "Error closing socket "+s+": "+ie );
                            }
                        }

                    } catch( IOException ie ) {

                        // On exception, remove this channel from the selector
                        key.cancel();

                        try {
                            sc.close();
                        } catch( IOException ie2 ) { System.out.println( ie2 ); }

                        System.out.println( "Closed "+sc );
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }

            // We remove the selected keys, because we've dealt
            // with them.
            keys.clear();
        }
    }
}