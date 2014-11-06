
-- Notes
/*No need to have: OR REPLACE FUNCTION:
read here: http://www.postgresql.org/docs/8.0/static/plpgsql.html
"Another way to avoid this problem is to use CREATE OR REPLACE FUNCTION when updating the definition of my_function (when a function is "replaced",
its OID is not changed)."
*/
-- abour RETURN QUERY and stuff (http://www.postgresql.org/docs/8.3/static/plpgsql-control-structures.html)
-- RETURN NEXT and RETURN QUERY do not actually return from the function — they simply append zero or more rows to the function's
-- result set. Execution then continues with the next statement in the PL/pgSQL function. As successive RETURN NEXT or RETURN QUERY
-- commands are executed, the result set is built up. A final RETURN, which should have no argument, causes control to exit the
-- function (or you can just let control reach the end of the function).

-- TODO /... should id's be positive??? I don't really see a reason on why
-- FIXME ... remove REPLACE FUNCTION from everywhere!

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
                        message text NOT NULL,

                        /* it doesn't make sense to send a message to yourself */
                        CONSTRAINT check_cannot_send_to_itself CHECK (sender_id != receiver_id)
                    );

  CREATE INDEX ON message (receiver_id, queue_id);
  CREATE INDEX ON message (sender_id);
  CREATE INDEX ON message (arrival_time);
END
$$ LANGUAGE plpgsql;

-- drops the database tables, this is done (instead of just deleting the entries
-- of the tables) in order to have all the id's starting from 1 again
-- FIXME is it needed after all?
CREATE FUNCTION clear_database() RETURNS void AS $$
BEGIN
  DROP TABLE client, queue, message;
  /* FIXME are indexed removed? */
END
$$ LANGUAGE plpgsql;

-- create a client and return the id of the newly created client
CREATE FUNCTION create_client(p_name varchar(20))
RETURNS integer AS $$
DECLARE
	inserted_client_id integer;
BEGIN
	INSERT INTO client (name) VALUES (p_name) RETURNING id INTO inserted_client_id;
	RETURN inserted_client_id;
END
$$ LANGUAGE plpgsql;


-- delete client given it's id
CREATE FUNCTION delete_client(p_client_id integer)
RETURNS void AS $$
BEGIN
	IF (SELECT count(id) FROM client WHERE id = p_client_id) != 0 THEN
		DELETE FROM client WHERE id = p_client_id;
	ELSE
		RAISE EXCEPTION 'DELETE_CLIENT: trying to delete a non existent client';
	END IF;
END
$$ LANGUAGE plpgsql;



-- Creates a queue and returns the id of the newly created queue
CREATE FUNCTION create_queue(p_name varchar(20))
  RETURNS integer AS $$
DECLARE
	inserted_queue_id integer;
BEGIN
	INSERT INTO queue(name) VALUES(p_name) RETURNING id INTO inserted_queue_id;
	RETURN inserted_queue_id;
END
$$ LANGUAGE plpgsql;

-- Deletes a queue given its id
CREATE FUNCTION delete_queue(p_queue_id integer)
  RETURNS void AS $$
BEGIN
  IF p_queue_id IS NULL THEN
    RAISE EXCEPTION 'DELETE_QUEUE: ILLEGAL ARGUMENT with p_queue_id being NULL';
  END IF;

	IF (SELECT count(id) FROM queue WHERE id = p_queue_id) != 0 THEN
		DELETE FROM queue WHERE id = p_queue_id;
	ELSE
		RAISE EXCEPTION 'DELETE_QUEUE: trying to delete a non existent queue';
	END IF;
END
$$ LANGUAGE plpgsql;