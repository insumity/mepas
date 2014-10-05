
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
                        message text NOT NULL CONSTRAINT check_length CHECK (LENGTH(message) IN (200, 2000)),

                        /* it doesn't make sense to send a message to yourself */
                        CONSTRAINT check_cannot_send_to_itself CHECK (sender_id != receiver_id)
                    );

  -- indexes on the primary keys are created by PostgresSQL as can be seen here (http://www.postgresql.org/docs/current/interactive/sql-createtable.html):
  -- PostgreSQL automatically creates an index for each unique constraint and primary key constraint to enforce uniqueness.
  -- Thus, it is not necessary to create an index explicitly for primary key columns. (See CREATE INDEX for more information.)
  CREATE INDEX ON message (queue_id, receiver_id);
  CREATE INDEX ON message (sender_id);
  CREATE INDEX ON message (arrival_time);
END
$$ LANGUAGE plpgsql;

-- drops the database tables, this is done (instead of just deleting the entries
-- of the tables) in order to have all the id's starting from 1 again
CREATE FUNCTION clear_database() RETURNS void AS $$
BEGIN
  DROP TABLE client, queue, message;
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
		RAISE EXCEPTION 'INVALID_CLIENT with (client_id)=(%)', p_client_id;
	END IF;
END
$$ LANGUAGE plpgsql;
