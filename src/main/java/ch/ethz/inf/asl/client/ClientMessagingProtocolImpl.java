package ch.ethz.inf.asl.client;

import ch.ethz.inf.asl.common.Message;
import ch.ethz.inf.asl.common.MessagingProtocol;
import ch.ethz.inf.asl.common.request.*;
import ch.ethz.inf.asl.common.response.*;
import ch.ethz.inf.asl.exceptions.MessageProtocolException;
import ch.ethz.inf.asl.logger.MyLogger;
import ch.ethz.inf.asl.utils.Optional;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static ch.ethz.inf.asl.utils.Helper.notNull;

public class ClientMessagingProtocolImpl extends MessagingProtocol {

    private int requestorId;

    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private MyLogger logger;


    public ClientMessagingProtocolImpl(int requestorId, Socket socket) throws IOException {
        notNull(socket, "Socket cannot be null");
        this.requestorId = requestorId;
        this.socket = socket;

//        this.logger = new MyLogger("Logger for: " + requestorId); // FIXME ... here?
        try {
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new MessageProtocolException("Client protocol couldn't be initialized", e);
        }
    }

    private void sendRequest(Request request) {
        try {
            byte[] data = objectToByteArray(request);

            dataOutputStream.write(data);
            dataOutputStream.flush(); //FIXME
        } catch (IOException e) {
            throw new MessageProtocolException("Request couldn't be sent", e);
        }
    }

    private Object byteArrayToObject(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bari = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bari);
        return ois.readObject();
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

    // perhaps put TODO Class<T> returnType as a parameter of this function
    private <R extends Response> R receiveResponse() {
        try {

            int length = dataInputStream.readInt();
            byte[] data = new byte[length];
            dataInputStream.read(data);
            Response response = (Response) byteArrayToObject(data);

            if (response == null) {
                throw new MessageProtocolException("Couldn't receive response, probably because a socket" +
                        "got closed!");
            }
            return (R) response;
        } catch (IOException e) {
            throw new MessageProtocolException("Response couldn't be received", e);
        } catch (ClassNotFoundException e) {
            throw new MessageProtocolException("Response wasn't of the appropriate type", e);
        }
    }

    @Override
    public int createQueue(String queueName) {
        Request request = new CreateQueueRequest(requestorId, queueName);
        sendRequest(request);
        CreateQueueResponse response = receiveResponse();
        return response.getQueueId();
    }

    @Override
    public void deleteQueue(int queueId) {

    }

    @Override
    public void sendMessage(int queueId, String content) {
        // receiverId == 0 inside request .. this is shit
        Request request = new SendMessageRequest(requestorId, queueId, content);
        sendRequest(request);
        Response response = receiveResponse();
        // what if there is an error in the response?
        // how do I know if the message was successfully sent? // TODO fixme
        return;
    }

    @Override
    public void sendMessage(int receiverId, int queueId, String content) {
        Request request = new SendMessageRequest(requestorId, receiverId, queueId, content);
        sendRequest(request);
        Response response = receiveResponse();
        return;
    }

    @Override
    public Optional<Message> receiveMessage(int queueId, boolean retrieveByArrivalTime) {
        Request request = new ReceiveMessageRequest(requestorId, queueId, retrieveByArrivalTime);
        sendRequest(request);
        ReceiveMessageResponse response = receiveResponse();
        if (response.getMessage() == null) {
            return Optional.empty();
        }
        else {
            return Optional.of(response.getMessage()); // FIXME, it;s all because optonal is not serializable
        }
    }

    @Override
    public Optional<Message> receiveMessage(int senderId, int queueId, boolean retrieveByArrivalTime) {
        Request request = new ReceiveMessageRequest(requestorId, senderId, queueId, retrieveByArrivalTime);
        sendRequest(request);
        ReceiveMessageResponse response = receiveResponse();
        if (response.getMessage() == null) {
            return Optional.empty();
        }
        else {
            return Optional.of(response.getMessage()); // FIXME, it;s all because optonal is not serializable
        }
    }

    @Override
    public Optional<Message> readMessage(int queueId, boolean retrieveByArrivalTime) {
        Request request = new ReadMessageRequest(requestorId, queueId, retrieveByArrivalTime);
        sendRequest(request);

        // TODO fixeme .. doesn't use ReadMessageResponse
        ReceiveMessageResponse response = receiveResponse();
        if (response.getMessage() == null) {
            return Optional.empty();
        }
        else {
            return Optional.of(response.getMessage()); // FIXME, it;s all because optonal is not serializable
        }
    }

    @Override
    public int[] listQueues() {
        Request request = new ListQueuesRequest(requestorId);
        sendRequest(request);
        ListQueuesResponse response = receiveResponse();
        return response.getQueues();
    }
}
