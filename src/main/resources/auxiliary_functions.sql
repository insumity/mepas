-- creates the client, queue and message tables as well as appropriate indexes
CREATE FUNCTION initialize_database() RETURNS void AS $$
BEGIN
  CREATE TABLE client (id serial primary key, name varchar(20) NOT NULL);
  CREATE TABLE queue (id serial primary key, name varchar(20) NOT NULL);

  CREATE TABLE message (id serial primary key,
                        sender_id integer REFERENCES client(id) NOT NULL, /* there is always a sender */
                        receiver_id integer REFERENCES client (id), /* can be NULL in case of no receiver, i.e. sent to everybody */
                        queue_id integer REFERENCES queue(id) NOT NULL, /* a message is always in a queue */
                        arrival_time timestamp NOT NULL, /* message always arrives at the queuing system */

                        /* a message can only contain 200 or 2000 characters */
                        message text NOT NULL CONSTRAINT check_length CHECK (LENGTH(message) <= 2000),

                        /* it doesn't make sense to send a message to yourself */
                        CONSTRAINT check_cannot_send_to_itself CHECK (sender_id != receiver_id)
                    );

  CREATE INDEX ON message (receiver_id, queue_id);
  CREATE INDEX ON message (sender_id);
  CREATE INDEX ON message (arrival_time);
END
$$ LANGUAGE plpgsql;


-- to be used by the experiment tester to quicker create a number of clients & queues in the system
CREATE FUNCTION create_clients(p_numclients integer)
  RETURNS void AS $$
BEGIN
  FOR i IN 1 .. p_numclients LOOP
    INSERT INTO client (name) VALUES  (i);
  END LOOP;
END
$$ LANGUAGE plpgsql;

CREATE FUNCTION create_queues(p_numqueues integer)
  RETURNS void AS $$
BEGIN
  FOR i IN 1 .. p_numqueues LOOP
    INSERT INTO queue (name) VALUES (i);
  END LOOP;
END
$$ LANGUAGE plpgsql;

