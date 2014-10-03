package ch.ethz.inf.asl.client;

import ch.ethz.inf.asl.common.Message;
import ch.ethz.inf.asl.common.MessagingProtocol;
import ch.ethz.inf.asl.common.request.*;
import ch.ethz.inf.asl.common.response.*;
import ch.ethz.inf.asl.exceptions.MessageProtocolException;
import ch.ethz.inf.asl.utils.Optional;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientMessagingProtocolImpl extends MessagingProtocol {

    private int requestorId;

    private Socket socket;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;

    public ClientMessagingProtocolImpl(int requestorId, Socket socket) {
        this.requestorId = requestorId;
        this.socket = socket;
        try {
            objectInputStream= new ObjectInputStream(socket.getInputStream());
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new MessageProtocolException("Client protocol couldn't be initialized", e);
        }
    }

    private void sendRequest(Request request) {
        try {
            objectOutputStream.writeObject(request);
        } catch (IOException e) {
            throw new MessageProtocolException("Request couldn't be sent", e);
        }
    }

    private <R extends Response> R receiveResponse() {
        try {
            Object response = objectInputStream.readObject();
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
