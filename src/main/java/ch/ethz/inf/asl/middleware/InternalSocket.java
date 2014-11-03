package ch.ethz.inf.asl.middleware;

import java.io.*;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import ch.ethz.inf.asl.utils.Pair;

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

    public DataOutputStream getOutputStream() {
        return oos;
    }

    public DataInputStream getInputStream() {
        return ois;
    }

    public boolean lengthIsKnown() {
        return lengthIsKnown.getFirst();
    }

    public int getBytesRead() {
        return bytesRead;
    }

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

