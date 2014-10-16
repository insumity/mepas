package ch.ethz.inf.asl.testutils;

import java.io.IOException;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

// Where is this class needed? This shouldn't be here for so many reasons!!! FIXME
public class InitializeDatabase {

    /* gets a connection for the given database */
    public static Connection getConnection(String host, int portNumber, String databaseName, String username, String password)
            throws SQLException, ClassNotFoundException {

        Class.forName("org.postgresql.Driver");
        String connectionURL = String.format("jdbc:postgresql://%s:%d/%s", host, portNumber, databaseName);
        return DriverManager.getConnection(connectionURL, username, password);
    }

    /* issues the command `psql -U username -d database -f filePath` and actually executes the SQL commands
       that reside in the file for the given database. Password is assumed to exist in the .pgpass file in the home directory of the user */
    private static void loadSQLFile(String username, String database, String filePath) throws IOException, InterruptedException {
        final String [] cmd = { "psql",
                "-U", username,
                "-d", database,
                "-f", filePath
        };

        try {
            Process process = Runtime.getRuntime().exec(cmd);
            if (process != null) {
                if (process.waitFor() != 0) {
                    System.err.println("Abnormal termination!");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /* drops the current database and creates one with the same name to be used by the middleware */
    public static void initialize(String host, int portNumber, String databaseName, String username, String password,
                                  int isolationLevel, String[] filePathsToBeExecutedForThisDatabase)
            throws ClassNotFoundException, SQLException, IOException, InterruptedException {

        final String INITIALIZE_DATABASE = "{ call initialize_database() }";
        String noDatabaseName = "";

        try (Connection connection = getConnection(host, portNumber, noDatabaseName, username, password)) {
            // drop the database just in case it's there
            connection.createStatement().execute("DROP DATABASE IF EXISTS " + databaseName);

            // create the database
            connection.createStatement().execute("CREATE DATABASE " + databaseName);
        }

        // load all the functions from `auxiliary_function.sql` and `***_basic_functions.sql` depending
        // on the isolation level
        loadSQLFile(username, databaseName, "src/main/resources/auxiliary_functions.sql");

        if (isolationLevel == Connection.TRANSACTION_READ_COMMITTED) {
            loadSQLFile(username, databaseName, "src/main/resources/read_committed_basic_functions.sql");
        }
        else if (isolationLevel == Connection.TRANSACTION_REPEATABLE_READ) {
            loadSQLFile(username, databaseName, "src/main/resources/repeatable_read_basic_functions.sql");
        }
        else {
            throw new IllegalArgumentException("Given level can only be READ_COMMITTED or REPEATABLE_READ!");
        }

        // create all the relations: queue, client and message
        try (Connection connection = getConnection(host, portNumber, databaseName, username, password);
             CallableStatement stmt = connection.prepareCall(INITIALIZE_DATABASE)) {

            stmt.execute();

            // verify that the tables were created
            // taken from: http://stackoverflow.com/questions/927807/how-can-i-detect-a-sql-tables-existence-in-java
            DatabaseMetaData md = connection.getMetaData();

            // verify data are there
            try (ResultSet rs = md.getTables(null, null, "%", null)) {
                List<String> tables = new LinkedList<>();
                while (rs.next()) {
                    tables.add(rs.getString(3));
                }

                assert (tables.contains("client"));
                assert (tables.contains("queue"));
                assert (tables.contains("message"));
            }
        }

        for (String filePath: filePathsToBeExecutedForThisDatabase) {
            loadSQLFile(username, databaseName, filePath);
        }
    }

}
