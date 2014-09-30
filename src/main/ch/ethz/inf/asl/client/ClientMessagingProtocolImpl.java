package ch.ethz.inf.asl.client;

import ch.ethz.inf.asl.Message;
import ch.ethz.inf.asl.MessagingProtocol;
import ch.ethz.inf.asl.common.Request;
import ch.ethz.inf.asl.common.Response;
import ch.ethz.inf.asl.utils.Optional;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ClientMessagingProtocolImpl extends MessagingProtocol {

    private int requestorId;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;

    public ClientMessagingProtocolImpl(int requestorId, ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream) {
        this.requestorId = requestorId;
        this.objectInputStream = objectInputStream;
        this.objectOutputStream = objectOutputStream;
    }

    private void sendRequest(Request request) {
        try {
            objectOutputStream.writeObject(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Response receiveResponse() {
        Object obj = null;
        try {
            obj = objectInputStream.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return (Response) obj;
    }

    @Override
    public int createQueue(String queueName) {

        // create request message
        Request request = new Request(requestorId).createQueue(queueName);

        // send message
        sendRequest(request);

        // read response
        Response response = receiveResponse();
        return response.getQueueId();
    }

    @Override
    public void deleteQueue(int queueId) {

    }

    @Override
    public void sendMessage(int queueId, String content) {
        // create request message
        Request request = new Request(requestorId).sendMessage(queueId, content);

        // send message
        sendRequest(request);

        // read response
        Response response = receiveResponse();
        // no TODO error check there is no ERROR
        return;
    }

    @Override
    public void sendMessage(int receiverId, int queueId, String content) {
// create request message
        Request request = new Request(requestorId).sendMessage(receiverId, queueId, content);

        // send message
        sendRequest(request);

        // read response
        Response response = receiveResponse();
        // no TODO error
        return;
    }

    @Override
    public Optional<Message> receiveMessage(int queueId, boolean retrieveByArrivalTime) {
        // create request message
        Request request = new Request(requestorId).receiveMessage(queueId, retrieveByArrivalTime);

        // send message
        sendRequest(request);

        // read response
        Response response = receiveResponse();
        // no TODO error
        return response.getMessage();
    }

    @Override
    public Optional<Message> receiveMessage(int senderId, int queueId, boolean retrieveByArrivalTime) {
        return null;
    }

    @Override
    public Optional<Message> readMessage(int queueId, boolean retrieveByArrivalTime) {
        return null;
    }

    @Override
    public int[] listQueues() {
        return new int[0];
    }
}
