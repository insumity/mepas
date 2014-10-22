package ch.ethz.inf.asl.middleware;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

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

        // THIS IS MADNESS
        oos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream(), BUFFER_SIZE));

        // those streams need to be used for the lifetime of the socket
        oos.flush();
        ois = new BufferedInputStream(socket.getInputStream(), BUFFER_SIZE);
    }

    private InternalSocket() {

    }

    private InternalSocket createInternalSocket(InternalSocket previousInternalSocket) {
        InternalSocket newInternalSocket = new InternalSocket();
        newInternalSocket.socket = previousInternalSocket.socket;
        newInternalSocket.lengthIsKnown = previousInternalSocket.lengthIsKnown;
        newInternalSocket.length = previousInternalSocket.length;
        newInternalSocket.bytesRead = previousInternalSocket.bytesRead;

        List<byte[]> list = new LinkedList<>();
        for (byte[] ar: previousInternalSocket.whatWasRead) {
            list.add(ar);
        }
        newInternalSocket.whatWasRead = list;

        newInternalSocket.oos = previousInternalSocket.oos;
        newInternalSocket.ois = previousInternalSocket.ois;
        return newInternalSocket;
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

    public int getBytesRead() {
        return bytesRead;
    }


    // FIXME in an attempt to make this class immutable
    private void internalSetBytesRead(int bytesRead) {
        this.bytesRead = bytesRead;
    }

    public InternalSocket setBytesRead(int bytesRead) {
        InternalSocket socket = createInternalSocket(this);
        socket.internalSetBytesRead(bytesRead);
        return socket;
    }

    public int getLength() {
        return length;
    }

    // FIXME in an attempt to make this class immutable
    private void internalSetLength(int length) {
        this.length = length;
        this.lengthIsKnown = true;
    }

    public InternalSocket setLength(int length) {
        InternalSocket socket = createInternalSocket(this);
        socket.internalSetLength(length);
        return socket;
    }

    public boolean readEverything() {
        return lengthIsKnown && (bytesRead == length);
    }

    // FIXME in an attempt to make this class immutable
    private void internalAddData(byte[] dataRead) {
        whatWasRead.add(dataRead);
    }

    public InternalSocket addData(byte[] dataRead) {
        InternalSocket socket = createInternalSocket(this);
        socket.internalAddData(dataRead);
        return socket;
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

    private void internalClean() {
        this.lengthIsKnown = false;
        this.bytesRead = 0;
        this.length = 0;
        this.whatWasRead.clear();
    }

    public InternalSocket clean() {
        InternalSocket socket = createInternalSocket(this);
        socket.internalClean();
        return socket;
    }

    @Override
    public boolean equals(Object other) {

        if (other instanceof InternalSocket) {
            InternalSocket otherSocket = (InternalSocket) other;
            return Objects.equals(this.socket, otherSocket.socket);
        }

        return false;
    }
}

