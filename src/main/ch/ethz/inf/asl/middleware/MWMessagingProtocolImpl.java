package ch.ethz.inf.asl.middleware;

import ch.ethz.inf.asl.common.Message;
import ch.ethz.inf.asl.common.MessageConstants;
import ch.ethz.inf.asl.exceptions.MessageProtocolException;
import ch.ethz.inf.asl.common.MessagingProtocol;
import ch.ethz.inf.asl.utils.Optional;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static ch.ethz.inf.asl.utils.Helper.hasText;

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
    public int createQueue(String queueName) {
        hasText(queueName, "queueName cannot be null or empty");

        if (queueName.length() > MessageConstants.MAXIMUM_QUEUE_NAME_LENGTH) {
            throw new IllegalArgumentException("queueName exceed max queue name length of: "
                + MessageConstants.MAXIMUM_QUEUE_NAME_LENGTH);
        }

        try (CallableStatement stmt = connection.prepareCall(CREATE_QUEUE)) {
            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.setString(2, queueName);
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

    private void sendMessageCommon(Optional<Integer> receiverId, int queueId, String content) {

        // TODO use constants, I have them in MessageConstants
        if (content.length() != 200 && content.length() != 2000) {
            throw new IllegalArgumentException("Given content has invalid length");
        }

        try (CallableStatement stmt = connection.prepareCall(SEND_MESSAGE)) {
            Date date = new Date();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String formattedDate = simpleDateFormat.format(date);
            Timestamp arrivalTime = Timestamp.valueOf(formattedDate);
            stmt.setInt(1, requestingUserId);

            if (receiverId.isPresent()) {
                stmt.setInt(2, receiverId.get());
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
        sendMessageCommon(Optional.<Integer>empty(), queueId, content);
    }

    @Override
    public void sendMessage(int receiverId, int queueId, String content) {
        sendMessageCommon(Optional.of(receiverId), queueId, content);
    }

    private Message getMessageFromResultSet(ResultSet resultSet) throws SQLException {
        resultSet.getInt(1); // corresponds to rowId, perhaps don't return it ... TODO FIXME
        int readSenderId = resultSet.getInt(2);
        Integer readReceiverId;

        int receiverId = resultSet.getInt(3);
        if (resultSet.wasNull()) {
            readReceiverId = null;
        }
        else {
            readReceiverId = receiverId;
        }

        int readQueueId = resultSet.getInt(4);
        Timestamp readArrivalTime = resultSet.getTimestamp(5);
        String readMessage = resultSet.getString(6);

        if (readReceiverId == null) { // duplicate code from above, I dpn't likeit FIXEME
            return new Message(readSenderId, readQueueId, readArrivalTime, readMessage);
        }
        else {
            return new Message(readSenderId, readReceiverId, readQueueId, readArrivalTime, readMessage);
        }
    }


    // TODO FIXME duplicate code ... when reading ResultSet
    @Override
    public Optional<Message> receiveMessage(int queueId, boolean retrieveByArrivalTime) {
        try (CallableStatement stmt = connection.prepareCall(RECEIVE_MESSAGE)) {
            stmt.setInt(1, requestingUserId);
            stmt.setInt(2, queueId);
            stmt.setBoolean(3, retrieveByArrivalTime);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                return Optional.empty();
            }

            Message readMessage = getMessageFromResultSet(rs);

            if (rs.next()) {
                // FIXME
                throw new MessageProtocolException("more than 2 messages received");
            }

            return Optional.of(readMessage);
        } catch (SQLException e) {
            throw new MessageProtocolException("failed to receive message", e);
        }
    }

    @Override
    public Optional<Message> receiveMessage(int senderId, int queueId, boolean retrieveByArrivalTime) {
        try (CallableStatement stmt = connection.prepareCall(RECEIVE_MESSAGE_FROM_SENDER)) {
            stmt.setInt(1, requestingUserId);
            stmt.setInt(2, senderId);
            stmt.setBoolean(3, retrieveByArrivalTime);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                return Optional.empty();
            }

            Message readMessage = getMessageFromResultSet(rs);

            if (rs.next()) {
                // FIXME
                throw new MessageProtocolException("more than 2 messages received");
            }

            return Optional.of(readMessage);
        } catch (SQLException e) {
            throw new MessageProtocolException("failed to receive message", e);
        }
    }

    @Override
    public Optional<Message> readMessage(int queueId, boolean retrieveByArrivalTime) {
        try (CallableStatement stmt = connection.prepareCall(READ_MESSAGE)) {
            stmt.setInt(1, requestingUserId);
            stmt.setInt(2, queueId);
            stmt.setBoolean(3, retrieveByArrivalTime);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                return Optional.empty();
            }

            Message readMessage = getMessageFromResultSet(rs);

            if (rs.next()) {
                // FIXME
                throw new MessageProtocolException("more than 2 messages read");
            }

            return Optional.of(readMessage);
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
        try (CallableStatement stmt = connection.prepareCall(LIST_QUEUES)) {
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