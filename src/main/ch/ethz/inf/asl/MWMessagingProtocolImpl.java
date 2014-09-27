package ch.ethz.inf.asl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

public class MWMessagingProtocolImpl extends MessagingProtocol {

    private int requestingUserId;
    private Connection connection;

    protected static final String CREATE_QUEUE = "{? = call create_queue(?)}";
    protected static final String DELETE_QUEUE = "{ call delete_queue(?) }";
    protected static final String SEND_MESSAGE = "{ call send_message(?, ?, ?, ?, ?) }";
    protected static final String RECEIVE_MESSAGE = "{ call receive_message(?, ?, ?) }";
    protected static final String READ_MESSAGE = "{ call read_message(?, ?, ?) }";
    protected static final String RECEIVE_MESSAGE_FROM_SENDER = "{ call receive_message_from_sender(?, ?, ?) }";
    protected static final String LIST_QUEUES = "{ call list_queues(?) }";

    public MWMessagingProtocolImpl(int requestingUserId, Connection connection) {
        this.requestingUserId = requestingUserId;
        this.connection = connection;
    }

    @Override
    public int createQueue() {
        try {
            CallableStatement stmt = connection.prepareCall(CREATE_QUEUE);
            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.execute();
            return stmt.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public void deleteQueue(int queueId) {

    }

    @Override
    public void sendMessage(int queueId, Message msg) {

    }

    @Override
    public void sendMessage(int receiverId, int queueId, Message msg) {

    }

    @Override
    public Message receiveMessage(int queueId, boolean retrieveByArrivalTime) {
        return null;
    }

    @Override
    public Message receiveMessage(int senderId, int queueId, boolean retrieveByArrivalTime) {
        return null;
    }

    @Override
    public Message readMessage(int queueId, boolean retrieveByArrivalTime) {
        return null;
    }

    @Override
    public int[] listQueues() {
        return new int[0];
    }
}
