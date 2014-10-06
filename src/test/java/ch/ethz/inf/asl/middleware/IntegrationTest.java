package ch.ethz.inf.asl.middleware;

import java.io.IOException;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

import static org.testng.Assert.assertTrue;

public class IntegrationTest {

    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "";
    private static final String HOST = "localhost";
    private static final Integer PORT_NUMBER = 5432;
    private static final String DB_NAME = "integrationtest";

    private static final String INITIALIZE_DATABASE = "{ call initialize_database() }";

    /* gets a connection for the given database */
    public static Connection getConnection(String database) throws SQLException, ClassNotFoundException {
        Class.forName("org.postgresql.Driver");
        return DriverManager.getConnection("jdbc:postgresql://" + HOST + ":" + PORT_NUMBER + "/" + database, USERNAME, PASSWORD);
    }

    /* issues the command `psql -U username -d database -f filePath` and actually executed the SQL commands
      that reside in the file */
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

    /* creates a database and fill it with tables and data */
    public static void initialize(boolean readCommittedLevel, boolean populateDatabaseWithMessages) throws ClassNotFoundException, SQLException, IOException, InterruptedException {

        try (Connection connection = getConnection("")) {
            // drop the database just in case it's there
            connection.createStatement().execute("DROP DATABASE IF EXISTS " + DB_NAME);

            // create the database
            connection.createStatement().execute("CREATE DATABASE " + DB_NAME);
        }

        // load all the functions from `auxiliary_function.sql` and `repeatable_read_basic_functions.sql`
        loadSQLFile(USERNAME, DB_NAME, "src/main/resources/auxiliary_functions.sql");

        if (readCommittedLevel) {
            loadSQLFile(USERNAME, DB_NAME, "src/main/resources/read_committed_basic_functions.sql");
        }
        else {
            loadSQLFile(USERNAME, DB_NAME, "src/main/resources/repeatable_read_basic_functions.sql");
        }

        // create all the relations: queue, client and message
        try (Connection connection = getConnection(DB_NAME);
             CallableStatement stmt = connection.prepareCall(INITIALIZE_DATABASE)) {

            stmt.execute();

            // verify that the tables were created
            // taken from: http://stackoverflow.com/questions/927807/how-can-i-detect-a-sql-tables-existence-in-java
            DatabaseMetaData md = connection.getMetaData();
            ResultSet rs = md.getTables(null, null, "%", null);
            List<String> tables = new LinkedList<>();
            while (rs.next()) {
                tables.add(rs.getString(3));
            }
            assertTrue(tables.contains("client"));
            assertTrue(tables.contains("queue"));
            assertTrue(tables.contains("message"));
        }


        // populate the tables
        loadSQLFile(USERNAME, DB_NAME, "src/test/resources/populate_database.sql");
        // TODO I don't like the way I do this hack here
        if (!populateDatabaseWithMessages) {
            try (Connection connection = getConnection(DB_NAME);
                 Statement stmt = connection.createStatement()) {
                stmt.execute("DELETE FROM message");
            }
        }
    }

    public static void tearDown() throws SQLException, ClassNotFoundException {
        // drop the database
        try (Connection connection = getConnection("")) {
            connection.createStatement().execute("DROP DATABASE " + DB_NAME);
        }
    }
}
