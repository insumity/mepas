package ch.ethz.inf.asl.client;

import ch.ethz.inf.asl.Message;
import ch.ethz.inf.asl.MessagingProtocol;
import ch.ethz.inf.asl.common.MessageType;
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

    @Override
    public int createQueue() {
        // creatQueueMessage


        // send message, request
        int requestorId;
        MessageType type;
        Object[] neededParameters;
        try {
            objectOutputStream.writeObject(new Object());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // read response
        try {
            objectInputStream.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    public void deleteQueue(int queueId) {

    }

    @Override
    public void sendMessage(int queueId, String content) {

    }

    @Override
    public void sendMessage(int receiverId, int queueId, String content) {

    }

    @Override
    public Optional<Message> receiveMessage(int queueId, boolean retrieveByArrivalTime) {
        return null;
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
