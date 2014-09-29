package ch.ethz.inf.asl.middleware;

import ch.ethz.inf.asl.Message;
import ch.ethz.inf.asl.MessageProtocolException;
import ch.ethz.inf.asl.MessagingProtocol;

import java.sql.*;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

public class MWMessagingProtocolImpl extends MessagingProtocol {

    private int requestingUserId;
    private Connection connection;

    static final String CREATE_QUEUE = "{ ? = call create_queue(?) }";
    static final String DELETE_QUEUE = "{ call delete_queue(?) }";
    static final String SEND_MESSAGE = "{ call send_message(?, ?, ?, ?, ?) }";
    static final String RECEIVE_MESSAGE = "{ call receive_message(?, ?, ?) }";
    static final String READ_MESSAGE = "{ call read_message(?, ?, ?) }";
    static final String RECEIVE_MESSAGE_FROM_SENDER = "{ call receive_message_from_sender(?, ?, ?) }";
    static final String LIST_QUEUES = "{ call list_queues(?) }";

    public MWMessagingProtocolImpl(int requestingUserId, Connection connection) {
        this.requestingUserId = requestingUserId;
        this.connection = connection;
    }

    @Override
    public int createQueue() {
        try (CallableStatement stmt = connection.prepareCall(CREATE_QUEUE)) {
            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.execute();
            return stmt.getInt(1);
        } catch (SQLException e) {
            throw new MessageProtocolException("failed to create queue", e);
        }
    }

    @Override
    public void deleteQueue(int queueId) {
        try (CallableStatement stmt = connection.prepareCall(DELETE_QUEUE)) {
            stmt.setInt(1, queueId);
            stmt.execute();
        } catch (SQLException e) {
            throw new MessageProtocolException("failed to delete queue", e);
        }
    }

    private void sendMessageCommon(Integer receiverId, int queueId, String content) {
        if (content.length() != 200 && content.length() != 2000) {
            throw new IllegalArgumentException("Given content has invalid length");
        }

        try (CallableStatement stmt = connection.prepareCall(MWMessagingProtocolImpl.SEND_MESSAGE)) {
            Timestamp arrivalTime = Timestamp.from(Instant.now());
            stmt.setInt(1, requestingUserId);

            if (receiverId != null) { // there is a receiver?? //FIXME
                stmt.setInt(2, receiverId);
            }
            else {
                stmt.setNull(2, Types.INTEGER);
            }

            stmt.setInt(3, queueId);
            stmt.setTimestamp(4, arrivalTime); // FIXME .. should be current Time ...  when message was received by the MW
            stmt.setString(5, content);
            stmt.execute();
        } catch (SQLException e) {
            throw new MessageProtocolException("failed to send message", e);
        }
    }

    @Override
    public void sendMessage(int queueId, String content) {
        sendMessageCommon(null, queueId, content);
    }

    @Override
    public void sendMessage(int receiverId, int queueId, String content) {
        // guaranteed that receiverId != null, it's primitive TODO
        sendMessageCommon(receiverId, queueId, content);
    }

    private Message getMessageFromResultSet(ResultSet resultSet) throws SQLException {
        resultSet.getInt(1); // corresponds to rowId, perhaps don't return it ... TODO FIXME
        int readSenderId = resultSet.getInt(2);
        Integer readReceiverId = resultSet.getInt(3);
        if (resultSet.wasNull()) {
            readReceiverId = null;
        }
        int readQueueId = resultSet.getInt(4);
        Timestamp readArrivalTime = resultSet.getTimestamp(5);
        String readMessage = resultSet.getString(6);

        return new Message(readSenderId, readReceiverId, readQueueId, readArrivalTime, readMessage);
    }


    // TODO FIXME duplicate code ... when reading ResultSet
    @Override
    public Message receiveMessage(int queueId, boolean retrieveByArrivalTime) {
        try (CallableStatement stmt = connection.prepareCall(MWMessagingProtocolImpl.RECEIVE_MESSAGE)) {
            stmt.setInt(1, requestingUserId);
            stmt.setInt(2, queueId);
            stmt.setBoolean(3, retrieveByArrivalTime);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                return null; // means there is no message FIXME .. I don't really like it
                // I could use optional but it's Java 8
            }

            Message readMessage = getMessageFromResultSet(rs);

            if (rs.next()) {
                // FIXME
                throw new MessageProtocolException("more than 2 messages received");
            }

            return readMessage;
        } catch (SQLException e) {
            throw new MessageProtocolException("failed to receive message", e);
        }
    }

    @Override
    public Message receiveMessage(int senderId, int queueId, boolean retrieveByArrivalTime) {
        try (CallableStatement stmt = connection.prepareCall(MWMessagingProtocolImpl.RECEIVE_MESSAGE_FROM_SENDER)) {
            stmt.setInt(1, requestingUserId);
            stmt.setInt(2, senderId);
            stmt.setBoolean(3, retrieveByArrivalTime);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                return null;
            }

            Message readMessage = getMessageFromResultSet(rs);

            if (rs.next()) {
                // FIXME
                throw new MessageProtocolException("more than 2 messages received");
            }

            return readMessage;
        } catch (SQLException e) {
            throw new MessageProtocolException("failed to receive message", e);
        }
    }

    @Override
    public Message readMessage(int queueId, boolean retrieveByArrivalTime) {
        try (CallableStatement stmt = connection.prepareCall(MWMessagingProtocolImpl.READ_MESSAGE)) {
            stmt.setInt(1, requestingUserId);
            stmt.setInt(2, queueId);
            stmt.setBoolean(3, retrieveByArrivalTime);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                return null;
            }

            Message readMessage = getMessageFromResultSet(rs);

            if (rs.next()) {
                // FIXME
                throw new MessageProtocolException("more than 2 messages read");
            }

            return readMessage;
        } catch (SQLException e) {
            throw new MessageProtocolException("failed to read message", e);
        }
    }

    // FIXME move up
    private int[] makeIntegerListToIntArray(List<Integer> list) {
        int[] array = new int[list.size()];
        int index = 0;
        for (Integer i: list) {
            array[index] = i;
            index++;
        }

        return array;
    }

    @Override
    public int[] listQueues() {
        try (CallableStatement stmt = connection.prepareCall(MWMessagingProtocolImpl.LIST_QUEUES)) {
            stmt.setInt(1, requestingUserId);
            ResultSet rs = stmt.executeQuery();

            List<Integer> queues = new LinkedList<>();
            while (rs.next()) {
                queues.add(rs.getInt(1));
            }

            return makeIntegerListToIntArray(queues);
        } catch (SQLException e) {
            throw new MessageProtocolException("failed to list queues", e);
        }
    }
}
