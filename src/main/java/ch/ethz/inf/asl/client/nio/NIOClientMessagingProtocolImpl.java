package ch.ethz.inf.asl.client.nio;

import ch.ethz.inf.asl.common.Message;
import ch.ethz.inf.asl.common.MessagingProtocol;
import ch.ethz.inf.asl.common.request.*;
import ch.ethz.inf.asl.common.response.*;
import ch.ethz.inf.asl.exceptions.MessageProtocolException;
import ch.ethz.inf.asl.utils.Optional;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class NIOClientMessagingProtocolImpl extends MessagingProtocol {

    private int requestorId;

    private SocketChannel socketChannel;

    public NIOClientMessagingProtocolImpl(int requestorId, SocketChannel socketChannel) {
        this.requestorId = requestorId;
        this.socketChannel = socketChannel;
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


    private void sendRequest(Request request) {
        try {
            byte[] dataToSend = objectToByteArray(request);
            ByteBuffer buffer = ByteBuffer.wrap(dataToSend);

            // fix ... all the bytes need to be writte
            int writtenBytes = socketChannel.write(buffer);
            System.err.println(writtenBytes);
        } catch (IOException e) {
            throw new MessageProtocolException("Request couldn't be sent", e);
        }
    }

    private Object byteArrayToObject(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bari = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bari);
        return ois.readObject();
    }

    private <R extends Response> R receiveResponse() {
        try {
            List<byte[]> readData = new LinkedList<>();
            int total = 0;
            int totalLength = -1;

            boolean firstTime = true;
            while (true) {
                ByteBuffer buffer = ByteBuffer.allocate(10);
                int bytesRead = socketChannel.read(buffer);

                if (bytesRead <= 0) {
                    break;
                }


                byte[] bytes = new byte[bytesRead];
                total += bytesRead;
                buffer.flip();
                buffer.get(bytes);

                if (firstTime && bytesRead >= 4) {
                    firstTime = false;
                    byte[] length = {bytes[0], bytes[1], bytes[2], bytes[3]};
                    totalLength = ByteBuffer.wrap(length).getInt();

                    byte[] semiBytes = new byte[bytesRead - 4];
                    for (int i = 0; i < bytesRead - 4; ++i) {
                        semiBytes[i] = bytes[i + 4];
                    }
                    bytes = semiBytes;
                }

                readData.add(bytes);

                if (bytesRead <= 0 || (total - 4) == totalLength) {
                    break;
                }

            }

            byte[] responseBytes = new byte[total];
            int j = 0;
            for (byte[] ar: readData) {
                for (int i = 0; i < ar.length; ++i) {
                    responseBytes[j] = ar[i];
                    ++j;
                }
            }

            Response response = (Response) byteArrayToObject(responseBytes);

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
        // TODO
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
