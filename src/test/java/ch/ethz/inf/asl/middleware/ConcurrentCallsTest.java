package ch.ethz.inf.asl.middleware;


import ch.ethz.inf.asl.utils.Utilities;
import org.postgresql.util.PSQLException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.sql.*;

import static ch.ethz.inf.asl.utils.TestConstants.INTEGRATION;

public class ConcurrentCallsTest {

    // the state code is handling serialization failures (which always return with a SQLSTATE value of '40001'), becau
    // you retrieve the errorMessage and then get compare it to "40001" from there. Cha cha :)

    private static final String INITIALIZE_DATABASE = "{ call initialize_database() }"; // TODO add tests for those guys
    private static final String CREATE_CLIENT = "{ ? = call create_client(?) }"; // TODO

    private Connection getConnection(String db) throws SQLException, ClassNotFoundException {
        Class.forName("org.postgresql.Driver");
        return DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/" + db, "bandwitch", "");
    }

    private void loadSQLFile(String username, String db, String filePath) throws IOException, InterruptedException {
        final String [] cmd = { "psql",
                "-U", username,
                "-d", db,
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

    // TODO .. add finally blocks here and in tearDown
    @BeforeMethod(groups = INTEGRATION)
    public void initialize() throws ClassNotFoundException, SQLException, IOException, InterruptedException {

        // create the database
        Connection connection = getConnection("");
        connection.createStatement().execute("CREATE DATABASE integrationtest");
        connection.close();


        // load all the functions from `auxiliary_function.sql` and `basic_functions.sql`
        System.err.println(System.getProperty("user.dir"));
        loadSQLFile("bandwitch", "integrationtest", "src/main/resources/auxiliary_functions.sql");
        loadSQLFile("bandwitch", "integrationtest", "src/main/resources/basic_functions.sql");

        // create all the relations: queue, client and message
        connection = getConnection("integrationtest");
        CallableStatement stmt = connection.prepareCall(INITIALIZE_DATABASE);
        stmt.execute();
        stmt.close();
        connection.close();

        // populate the tables
        loadSQLFile("bandwitch", "integrationtest", "src/test/resources/populate_database.sql");
    }

    @AfterMethod(groups = INTEGRATION)
    public void tearDown() throws SQLException, ClassNotFoundException {
        // drop the database
        Connection connection = getConnection("");
        connection.createStatement().execute("DROP DATABASE integrationtest");
        connection.close();
    }

    @Test(groups = INTEGRATION)
    public void test() throws SQLException, ClassNotFoundException {
        Connection connection = getConnection("integrationtest");
        final String CONCURRENT_UPDATE_ERROR_SQL_STATE = "40001";

        System.err.println("READ_UNCOMITTED: " + Connection.TRANSACTION_READ_UNCOMMITTED);
        System.err.println("READ_COMMITTED: " + Connection.TRANSACTION_READ_COMMITTED);
        System.err.println("REPEATABLE_READ: " + Connection.TRANSACTION_REPEATABLE_READ);
        System.err.println("SERIALIZABLE: " + Connection.TRANSACTION_SERIALIZABLE);
        System.out.println("Current isolation level: " + connection.getTransactionIsolation());
//        connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
        System.out.println("Current isolation level: " + connection.getTransactionIsolation());
        connection.close();

        final int CONSTANT = 1000;

        Thread a = new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                while (i < CONSTANT) {
                    try {
                        Connection connection = getConnection("integrationtest");
                        CallableStatement stmt = connection.prepareCall(MWMessagingProtocolImpl.SEND_MESSAGE);

                        int senderId = 2;
                        int queueId = 1;

                        Timestamp arrivalTime = Timestamp.valueOf("2014-12-12 12:34:12");
                        String message = Utilities.createStringWith(200, 'A');
                        stmt.setInt(1, senderId);
                        stmt.setNull(2, Types.INTEGER);
                        stmt.setInt(3, queueId);
                        stmt.setTimestamp(4, arrivalTime);
                        stmt.setString(5, message);
                        stmt.execute();
                        stmt.close();
                        connection.close();

                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    i++;
                }
            }
        });

        final int[] data = new int[2];

        Thread b = new Thread(new Runnable() {

            int times = 0;

            @Override
            public void run() {
                int i = 0;
                while (i < CONSTANT) {
                    try {
                        Connection connection = getConnection("integrationtest");
                        connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
                        CallableStatement stmt = connection.prepareCall(MWMessagingProtocolImpl.RECEIVE_MESSAGE);

                        boolean retrieveByArrivalTime = false;
                        stmt.setInt(1, 3);
                        stmt.setInt(2, 1);
                        stmt.setBoolean(3, retrieveByArrivalTime);

                        ResultSet rs = null;
                        try {
                            rs = stmt.executeQuery();
                        } catch (PSQLException e) {
                            if (e.getServerErrorMessage().getSQLState().equals(CONCURRENT_UPDATE_ERROR_SQL_STATE)) {
                                stmt.close();
                                connection.close();
                                continue;
                            }
                        }
                        boolean thereAreRows = rs.next();
                        if (thereAreRows) {
                            times++;
                        }

                        stmt.close();
                        connection.close();

                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    i++;
                    if (i == CONSTANT) {
                        data[1] = times;
                    }
                }
            }
        });

        Thread c = new Thread(new Runnable() {

            int times = 0;

            @Override
            public void run() {
                int i = 0;
                while (i < CONSTANT) {
                    try {
                        Connection connection = getConnection("integrationtest");
                        connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
                        CallableStatement stmt = connection.prepareCall(MWMessagingProtocolImpl.RECEIVE_MESSAGE);

                        boolean retrieveByArrivalTime = false;
                        stmt.setInt(1, 4);
                        stmt.setInt(2, 1);
                        stmt.setBoolean(3, retrieveByArrivalTime);

                        ResultSet rs = null;
                        try {
                            rs = stmt.executeQuery();
                        } catch (PSQLException e) {
                            if (e.getServerErrorMessage().getSQLState().equals(CONCURRENT_UPDATE_ERROR_SQL_STATE)) {
                                stmt.close();
                                connection.close();
                                continue;
                            }
                            else {
                                throw new IllegalStateException("Whatever!");
                            }
                        }

                        boolean thereAreRows = rs.next();

                        if (thereAreRows) {
                            times++;
                        }

                        stmt.close();
                        connection.close();

                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    i++;
                    if (i == CONSTANT) {
                        data[0] = times;
                    }
                }
            }
        });

        a.start();

        try {
            // do all the insertions, i.e. sending of messages
            a.join();

            // shouldn't we start b and c after a finishes?
            b.start();
            c.start();

            b.join();
            c.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(data[0] + " + " + data[1] + " = " + (data[0] + data[1]));
    }
}
