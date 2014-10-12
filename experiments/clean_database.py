import psycopg2
from subprocess import call

def connect_database(host, dbname, user, password):
    connection_string = "host='" + host + "' dbname='" + dbname + "' user='" + \
                  user + "' password='" + password + "'"

    conn = psycopg2.connect(connection_string)
    return conn

# drops and recreates the database dbname
# nobody should be connected to the database when this function is executed
def recreate_database(host, dbname, user, password):

    # connect to default "postgres" database, this is done
    # because you cannot delete dbName while on it, so we cannot to another database
    # and issue "DROP DATABASE" from there
    conn = connect_database(host, "postgres", user, password)
    conn.set_isolation_level(psycopg2.extensions.ISOLATION_LEVEL_AUTOCOMMIT)
    cursor = conn.cursor()
    cursor.execute("DROP DATABASE IF EXISTS " + dbname)
    cursor.execute("CREATE DATABASE " + dbname)
    conn.set_isolation_level(psycopg2.extensions.ISOLATION_LEVEL_READ_COMMITTED)
    return

def initialize_database(host, dbname, user, password, numberOfClients, numberOfQueues, file):
    # auxiliary_functions.sql // create_client initialize_database
    # read_committed_basic_functions.sql
    call(["psql", "-h", host, "-U", user, "-d", dbname, "-f", file])
    conn = connect_database(host, dbname, user, password)
    cursor = conn.cursor()
    conn.set_isolation_level(psycopg2.extensions.ISOLATION_LEVEL_AUTOCOMMIT)
    cursor.callproc("initialize_database")

    for client in range(0, numberOfClients):
        cursor.callproc("create_client", ["client" + str(client + 1).zfill(3)])

    for queue in range(0, numberOfQueues):
        cursor.callproc("create_queue", ["queue" + str(queue + 1).zfill(3)])

    conn.set_isolation_level(psycopg2.extensions.ISOLATION_LEVEL_READ_COMMITTED)
    return
