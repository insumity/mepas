import psycopg2
from subprocess import call


class Database:
    def __init__(self, databaseHost, databasePortNumber, databaseName, databaseUsername, databasePassword):
        self.databaseHost = databaseHost
        self.databasePortNumber = databasePortNumber
        self.databaseName = databaseName
        self.databaseUsername = databaseUsername
        self.databasePassword = databasePassword

    description = "This class corresponds to a connection to a database and can" \
                  "be used to clean and initialize this database."

    def __connectToDatabase(self, databaseName):
        connection = psycopg2.connect(
            database=databaseName, user=self.databaseUsername, password=self.databasePassword,
            host=self.databaseHost, port=self.databasePortNumber)
        return connection

    # drops and recreates the database with databaseName
    # nobody should be connected to the database when this function is executed
    def recreateDatabase(self):
        # connect to default "postgres" database, this is done because you cannot delete
        # databaseName while on it, so we connect to another database
        # and issue "DROP DATABASE" from there
        connection = self.__connectToDatabase("postgres")
        connection.set_isolation_level(psycopg2.extensions.ISOLATION_LEVEL_AUTOCOMMIT)
        cursor = connection.cursor()
        cursor.execute("DROP DATABASE IF EXISTS " + self.databaseName)
        cursor.execute("CREATE DATABASE " + self.databaseName)
        connection.set_isolation_level(psycopg2.extensions.ISOLATION_LEVEL_READ_COMMITTED)
        return


    def initializeDatabase(self, numberOfClients, numberOfQueues, initializationFiles):
        for file in initializationFiles:
            call(["psql", "-h", self.databaseHost, "-p", str(self.databasePortNumber),
                  "-U", self.databaseUsername, "-d", self.databaseName, "-f", file])

        connection = self.__connectToDatabase(self.databaseName)
        cursor = connection.cursor()
        connection.set_isolation_level(psycopg2.extensions.ISOLATION_LEVEL_AUTOCOMMIT)
        cursor.callproc("initialize_database")

        for client in range(0, numberOfClients):
            cursor.callproc("create_client", ["client" + str(client + 1).zfill(3)])

        for queue in range(0, numberOfQueues):
            cursor.callproc("create_queue", ["queue" + str(queue + 1).zfill(3)])

        connection.set_isolation_level(psycopg2.extensions.ISOLATION_LEVEL_READ_COMMITTED)
        return

