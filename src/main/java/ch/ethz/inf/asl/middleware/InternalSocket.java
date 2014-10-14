package ch.ethz.inf.asl.middleware;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class InternalSocket {

    private Socket socket;

    // input and output streams associated with the socket
    private DataOutputStream oos;
    private BufferedInputStream ois; // why isn't this DataInputStream ? FIXME

    private int bytesRead;

    // in case lengthIsKnown is true, length contains the length of the upcoming object,
    // otherwise its value is useless
    private boolean lengthIsKnown;
    private int length;

    private List<byte[]> whatWasRead;

    private static final int BUFFER_SIZE = 100;


    public InternalSocket(Socket socket) throws IOException {
        this.socket = socket;
        this.lengthIsKnown = false;
        whatWasRead = new LinkedList<>();

        // THIS IS MADDNESS
        oos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream(), BUFFER_SIZE));

        // those strems need to be used for the lifetime of the socket
        oos.flush();
        ois = new BufferedInputStream(socket.getInputStream(), BUFFER_SIZE);
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

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
        this.lengthIsKnown = true;
    }

    public boolean readEverything() {
        return lengthIsKnown && (bytesRead == length);
    }

    public void addData(byte[] dataRead) {
        whatWasRead.add(dataRead);
    }

    public byte[] getObjectData() {
        byte[] objectData = new byte[length];

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
        this.length = 0;
        this.whatWasRead.clear();
    }

    @Override
    public boolean equals(Object other) {

        System.err.println("equals was called with " + this + ", " + other);

        if (other instanceof InternalSocket) {
            InternalSocket otherSocket = (InternalSocket) other;
            return socket.equals(otherSocket.socket);
        }

        return false;
    }
}

