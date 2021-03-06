package ch.ethz.inf.asl.client;

import ch.ethz.inf.asl.common.Message;
import ch.ethz.inf.asl.common.MessagingProtocol;
import ch.ethz.inf.asl.common.request.*;
import ch.ethz.inf.asl.common.response.*;
import ch.ethz.inf.asl.exceptions.MessagingProtocolException;
import ch.ethz.inf.asl.utils.Helper;
import ch.ethz.inf.asl.utils.Optional;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import static ch.ethz.inf.asl.utils.Verifier.notNull;

public class ClientMessagingProtocolImpl implements MessagingProtocol {

    private int requestorId;

    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    // for end-to-end testing
    private List<Request> sentRequests;
    private List<Response> receivedResponses;
    private boolean isEndToEndTest = false;


    public List<Request> getSentRequests() {
        return sentRequests;
    }

    public List<Response> getReceivedResponses() {
        return receivedResponses;
    }

    public ClientMessagingProtocolImpl(Socket socket, int requestorId, boolean isEndToEndTest) throws IOException {
        notNull(socket, "Given socket cannot be null!");

        this.requestorId = requestorId;

        try {
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new MessagingProtocolException("Client protocol couldn't be initialized", e);
        }


        sentRequests = new LinkedList<>();
        receivedResponses = new LinkedList<>();
        this.isEndToEndTest = isEndToEndTest;
    }

    // send a request to the middleware through the underlying socket
    protected void sendRequest(Request request) {
        try {
            byte[] data = Helper.serialize(request);

            dataOutputStream.write(data);
            dataOutputStream.flush();
            if (isEndToEndTest) {
                sentRequests.add(request);
            }
        } catch (IOException e) {
            throw new MessagingProtocolException("Request couldn't be sent", e);
        }
    }

    // receives a response from the middleware through the underlying socket
    protected <R extends Response> R receiveResponse() {
        try {

            int length = dataInputStream.readInt();
            byte[] data = new byte[length];
            byte[] lengthToByteArray = ByteBuffer.allocate(4).putInt(length).array();
            dataInputStream.readFully(data);

            byte[] concatenated = Helper.concatenate(lengthToByteArray, data);
            Response response = (Response) Helper.deserialize(concatenated);

            if (isEndToEndTest) {
                receivedResponses.add(response);
            }

            if (response == null) {
                throw new MessagingProtocolException("Couldn't receive response, probably because a socket" +
                        "got closed!");
            }

            // INFORM Client there was a problem
            if (!response.isSuccessful()) {
                throw new MessagingProtocolException(response.getFailedMessage());
            }

            return (R) response;
        } catch (IOException e) {
            throw new MessagingProtocolException("Response couldn't be received", e);
        } catch (ClassNotFoundException e) {
            throw new MessagingProtocolException("Response wasn't of the appropriate type", e);
        }
    }

    @Override
    public int sayHello(String clientName) {
        Request request = new SayHelloRequest(clientName);
        sendRequest(request);
        SayHelloResponse response = receiveResponse();
        return response.getClientId();
    }

    @Override
    public void sayGoodbye() {
        Request request = new SayGoodbyeRequest(requestorId);
        sendRequest(request);
        receiveResponse();
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
        Request request = new DeleteQueueRequest(requestorId, queueId);
        sendRequest(request);
        receiveResponse();
    }

    @Override
    public void sendMessage(int queueId, String content) {
        // receiverId == 0 inside request .. this is shit
        Request request = new SendMessageRequest(requestorId, queueId, content);
        sendRequest(request);
        receiveResponse();
    }

    @Override
    public void sendMessage(int receiverId, int queueId, String content) {
        Request request = new SendMessageRequest(requestorId, receiverId, queueId, content);
        sendRequest(request);
        receiveResponse();
    }

    private Optional<Message> receiveMessageCommon(Integer senderId, int queueId, boolean retrieveByArrivalTime) {
        Request request;
        if (senderId == null) {
            request = new ReceiveMessageRequest(requestorId, queueId, retrieveByArrivalTime);
        }
        else {
            request = new ReceiveMessageRequest(requestorId, senderId, queueId, retrieveByArrivalTime);
        }

        sendRequest(request);
        ReceiveMessageResponse response = receiveResponse();
        if (response.getMessage() == null) {
            return Optional.empty();
        }
        else {
            return Optional.of(response.getMessage());
        }
    }

    @Override
    public Optional<Message> receiveMessage(int queueId, boolean retrieveByArrivalTime) {
        return receiveMessageCommon(null, queueId, retrieveByArrivalTime);
    }

    @Override
    public Optional<Message> receiveMessage(int senderId, int queueId, boolean retrieveByArrivalTime) {
        return receiveMessageCommon(senderId, queueId, retrieveByArrivalTime);
    }

    @Override
    public Optional<Message> readMessage(int queueId, boolean retrieveByArrivalTime) {
        Request request = new ReadMessageRequest(requestorId, queueId, retrieveByArrivalTime);
        sendRequest(request);
        ReadMessageResponse response = receiveResponse();
        if (response.getMessage() == null) {
            return Optional.empty();
        }
        else {
            return Optional.of(response.getMessage());
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
