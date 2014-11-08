package ch.ethz.inf.asl.middleware;

import ch.ethz.inf.asl.utils.Pair;

import java.io.*;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * This class implements an internal socket. This socket is to be used
 * together with {@link ch.ethz.inf.asl.middleware.MiddlewareRunnable}.
 */
public class InternalSocket {

    private Socket socket;

    // input and output streams associated with the socket
    private DataOutputStream oos;
    private DataInputStream ois;

    private int bytesRead;

    public int timesEntered;
    public int timesToReadARequest;

    // in case lengthIsKnown is true, length contains the length of the upcoming object,
    // otherwise its value is useless
    private long lastTime = 0;
    private Pair<Boolean, Integer> lengthIsKnown;

    private List<byte[]> whatWasRead;

    /**
     * Initialized a new InternalSocket object based on the given socket.
     * @param socket socket to be used for the creation of the internal socket
     * @throws IOException in case socket is not connected or an I/O error occurs
     */
    public InternalSocket(Socket socket) throws IOException {
        this.socket = socket;

        this.lengthIsKnown = new Pair();
        this.lengthIsKnown.setFirst(false);
        this.lastTime = System.currentTimeMillis();

        this.timesEntered = 0;

        this.whatWasRead = new LinkedList<>();

        final int BUFFER_SIZE = 64;

        // those streams are going to be used for the lifetime of the socket
        this.oos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream(), BUFFER_SIZE));
        this.ois = new DataInputStream(new BufferedInputStream(socket.getInputStream(), BUFFER_SIZE));
    }


    public long getLastTime() {
        return lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    /**
     * Gets the output stream of this internal socket.
     * @return output stream of the socket
     */
    public DataOutputStream getOutputStream() {
        return oos;
    }

    /**
     * Gets the input stream of this internal socket.
     * @return input stream of the socket
     */
    public DataInputStream getInputStream() {
        return ois;
    }

    public boolean lengthIsKnown() {
        return lengthIsKnown.getFirst();
    }

    /**
     * Gets the number of bytes that have been read until now from the current socket.
     * @return number of bytes read
     */
    public int getBytesRead() {
        return bytesRead;
    }

    /**
     * Sets the number of bytes that have been read until now by this socket.
     * @param bytesRead
     */
    public void setBytesRead(int bytesRead) {
        this.bytesRead = bytesRead;
    }

    public int getLength() {
        return lengthIsKnown.getSecond();
    }

    public void setLength(int length) {
        this.lengthIsKnown = new Pair(true, length);
    }

    public boolean readEverything() {
        return lengthIsKnown.getFirst() && (bytesRead == lengthIsKnown.getSecond());
    }

    public void addData(byte[] dataRead) {
        whatWasRead.add(dataRead);
    }

    public byte[] getObjectData() {
        byte[] objectData = new byte[lengthIsKnown.getSecond()];

        int j = 0;
        for (byte[] ar: whatWasRead) {
            for (int i = 0; i < ar.length; ++i) {
                objectData[j] = ar[i];
                ++j;
            }
        }

        return objectData;
    }

    /**
     * Cleans what has been read by the current socket. This means that a new serialized object
     * can be read now.
     */
    public void clean() {
        this.lengthIsKnown = new Pair(false, 0);
        this.bytesRead = 0;
        this.whatWasRead.clear();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof InternalSocket) {
            InternalSocket otherSocket = (InternalSocket) other;
            return socket.equals(otherSocket.socket);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(socket);
    }
}

