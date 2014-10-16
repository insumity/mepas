package ch.ethz.inf.asl.client;

import ch.ethz.inf.asl.common.Message;
import ch.ethz.inf.asl.common.MessagingProtocol;
import ch.ethz.inf.asl.common.request.*;
import ch.ethz.inf.asl.common.response.*;
import ch.ethz.inf.asl.exceptions.MessageProtocolException;
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

public class ClientMessagingProtocolImpl extends MessagingProtocol {

    private boolean clientIsInTheSystem = false;
    private int requestorId;

    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    // for end-to-end testing
    private List<Request> sentRequests;
    private List<Response> receivedResponses;
    private boolean saveEverything = false;

    public List<Request> getSentRequests() {
        return sentRequests;
    }

    public List<Response> getReceivedResponses() {
        return receivedResponses;
    }

    public ClientMessagingProtocolImpl(Socket socket, boolean saveEverything) throws IOException {
        notNull(socket, "Given socket cannot be null!");
        this.socket = socket;

        try {
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new MessageProtocolException("Client protocol couldn't be initialized", e);
        }


        sentRequests = new LinkedList<>();
        receivedResponses = new LinkedList<>();
        this.saveEverything = saveEverything;
    }

    private void sendRequest(Request request) {
        if (!(request instanceof SayHelloRequest) && !clientIsInTheSystem) {
            // TODO have a nicer exeption
            throw new IllegalArgumentException("The client is not in the system yet! Try saying hello first!");
        }
        try {
            byte[] data = Helper.serialize(request);

            dataOutputStream.write(data);
            dataOutputStream.flush(); //FIXME

            if (saveEverything) {
                sentRequests.add(request);
            }
        } catch (IOException e) {
            throw new MessageProtocolException("Request couldn't be sent", e);
        }
    }

    // perhaps put TODO Class<T> returnType as a parameter of this function
    private <R extends Response> R receiveResponse() {
        try {
            int length = dataInputStream.readInt();
            byte[] data = new byte[length];
            // FIXME might return less bytes
            // hacky fix
            byte[] lengthToByteArray = ByteBuffer.allocate(4).putInt(length).array();
            dataInputStream.read(data);
            byte[] concatented = Helper.concatenate(lengthToByteArray, data);
            Response response = (Response) Helper.deserialize(concatented);

            if (saveEverything) {
                receivedResponses.add(response);
            }

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

    private void setRequestorId(int requestorId) {
        this.requestorId = requestorId;
        this.clientIsInTheSystem = true;
    }

    @Override
    public int sayHello(String clientName) {
        Request request = new SayHelloRequest(clientName);
        sendRequest(request);
        SayHelloResponse response = receiveResponse();
        int requestorId = response.getClientId();
        setRequestorId(requestorId);
        return requestorId;
    }

    @Override
    public void sayGoodbye() {
        Request request = new GoodbyeRequest(requestorId);
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
