/* The following functions implement the basic functionality of the system.
   The functions delete_queue, receive_message, receive_message_from_sender assume
   that there are being executed with REPEATABLE_READ transactional isolation level.
 */

-- Sends a message by inserting message in the relation.
-- In case receiver_id is NULL this means there is no specific receiver
CREATE FUNCTION send_message(p_sender_id integer, p_receiver_id integer, p_queue_id integer,
  p_arrival_time timestamp, p_message text)
RETURNS void AS $$
BEGIN
    INSERT INTO message (sender_id, receiver_id, queue_id, arrival_time, message)
      VALUES (p_sender_id, p_receiver_id, p_queue_id, p_arrival_time, p_message);
END;
$$ LANGUAGE plpgsql;


-- type of a message that is included in the message relation
CREATE TYPE message_type AS (id integer, sender_id integer, receiver_id integer, queue_id integer,
  arrival_time timestamp, message text);


-- Reads a message similar to receive_message below but without actually removing the message from the queue.
-- The returned message could be possibly read multiple times until a receive_message is called that
-- will actually remove the message
CREATE FUNCTION read_message(
  p_requesting_user_id integer, /* id of the user issuing the read message request */
  p_queue_id integer, /* id of the queue from where the message is retrieved */
  p_retrieve_by_arrival_time boolean /* if true returns the newest, i.e. one closest to the current time,
                                        message based on its timestamp */
)
  RETURNS SETOF message_type AS $$
DECLARE
	received_message_id integer;
BEGIN
  IF p_requesting_user_id IS NULL THEN
    RAISE EXCEPTION 'READ_MESSAGE: ILLEGAL ARGUMENT with p_requesting_user_id being NULL';
  END IF;

	IF p_retrieve_by_arrival_time = TRUE THEN
		RETURN QUERY SELECT * FROM message
                    WHERE queue_id = p_queue_id AND (receiver_id = p_requesting_user_id
                                  /* don't read messages you sent, in case receiver_id is NULL it could be that you
                                     are the sender of the message. In case receiver_id is not NULL we know for sure
                                     that sender_id != receiver_id because of the `check_cannot_send_to_itself`
                                     constraint in the message relation */
                                     OR (receiver_id IS NULL AND sender_id != p_requesting_user_id))
                    ORDER BY arrival_time DESC LIMIT 1;
	ELSE
		RETURN QUERY SELECT * FROM message
                    WHERE queue_id = p_queue_id AND (receiver_id = p_requesting_user_id
                                     OR (receiver_id IS NULL AND sender_id != p_requesting_user_id))
                    LIMIT 1;
	END IF;
END;
$$ LANGUAGE plpgsql;


-- Receives a message from a specific queue while removing the message from the queue.
-- The received message that is returned can be retrieved based on the time
-- of the arrival of the message. The received message could have as a receiver the user issuing
-- the request or be a message sent to everybody, i.e. its receiver_id IS NULL
CREATE FUNCTION receive_message(
  p_requesting_user_id integer, /* id of the user issuing the receive message request */
  p_queue_id integer, /* id of the queue from where the message is retrieved */
  p_retrieve_by_arrival_time boolean /* if true returns the newest, i.e. one closest to the current time,
                                        message based on its timestamp */

)
  RETURNS SETOF message_type AS $$
DECLARE
  received_message_id integer;
BEGIN
  IF p_requesting_user_id IS NULL THEN
    RAISE EXCEPTION 'RECEIVE_MESSAGE: ILLEGAL ARGUMENT with p_requesting_user_id being NULL';
  END IF;

  SELECT id INTO received_message_id FROM read_message(p_requesting_user_id, p_queue_id, p_retrieve_by_arrival_time);
  -- TODO: (?) why isn't it in read_committed the same? I mean call read_message
  RETURN QUERY SELECT * FROM message WHERE id = received_message_id;

  DELETE FROM message where id = received_message_id;
END;
$$ LANGUAGE plpgsql;


-- Receives a message similar to receive_message but with the extra check that the message was sent
-- by a specific sender. This function doesn't ask for a queue id, this is done because a user of this
-- function is assumed to be interested in receiving a message in general from this sender without
-- caring for the queue from which the message is received
CREATE FUNCTION receive_message_from_sender(
  p_requesting_user_id INTEGER, /* id of the user issuing the receive message from sender request */
  p_sender_id          INTEGER, /* id of the sender of the message */
  p_retrieve_by_arrival_time BOOLEAN /* if true returns the newest, i.e. one closest to the current time,
                                        message based on its timestamp */
)
RETURNS SETOF message_type AS $$
DECLARE
  received_message_id integer;
BEGIN
  IF p_requesting_user_id IS NULL THEN
    RAISE EXCEPTION 'RECEIVE_MESSAGE_FROM_SENDER: ILLEGAL ARGUMENT with p_requesting_user_id being NULL';
  END IF;

  /* doesn't make sense to want to receive a message sent by you */
  IF p_requesting_user_id = p_sender_id THEN
     RAISE EXCEPTION 'RECEIVE_MESSAGE_FROM_SENDER: sender id cannot be the same as the one issuing the request';
  END IF;

  IF p_retrieve_by_arrival_time = TRUE THEN
    SELECT id INTO received_message_id FROM message
          WHERE sender_id = p_sender_id AND (receiver_id = p_requesting_user_id OR receiver_id IS NULL)
          ORDER BY arrival_time DESC LIMIT 1;

    RETURN QUERY SELECT * FROM message WHERE id = received_message_id;
  ELSE
    SELECT id INTO received_message_id FROM message
          WHERE sender_id = p_sender_id AND (receiver_id = p_requesting_user_id OR receiver_id IS NULL)
          LIMIT 1;

    RETURN QUERY SELECT * FROM message WHERE id = received_message_id;
  END IF;

  DELETE FROM message WHERE id = received_message_id;
END;
$$ LANGUAGE plpgsql;


-- Lists queues where a message for the specific user exists. Also messages with no
-- specific receiver are considered by this function. So if a queue contains just one
-- message with no specific receiver, i.e. it's receiver_id IS NULL this queue's id will
-- also be returned.
CREATE FUNCTION list_queues(
  p_requesting_user_id INTEGER /* id of the user issuing the list_queues request */
)
RETURNS TABLE(r_queue_id integer) AS $$
BEGIN
  IF p_requesting_user_id IS NULL THEN
    RAISE EXCEPTION 'LIST_QUEUES: ILLEGAL ARGUMENT with p_requesting_user_id being NULL';
  END IF;

  -- DISTINCT so you don't get the same queue id multiple times
  RETURN QUERY SELECT DISTINCT queue_id FROM message
                  WHERE receiver_id = p_requesting_user_id OR (receiver_id IS NULL AND sender_id != p_requesting_user_id);
END;
$$ LANGUAGE plpgsql;
