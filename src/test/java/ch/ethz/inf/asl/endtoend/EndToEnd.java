package ch.ethz.inf.asl.endtoend;

import ch.ethz.inf.asl.client.Client;
import ch.ethz.inf.asl.common.ReadConfiguration;
import ch.ethz.inf.asl.common.request.Request;
import ch.ethz.inf.asl.common.response.Response;
import ch.ethz.inf.asl.middleware.Middleware;
import ch.ethz.inf.asl.testutils.InitializeDatabase;
import org.testng.annotations.Test;

import java.io.IOException;
import java.security.Permission;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static ch.ethz.inf.asl.testutils.TestConstants.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

/**
 * End to end testing for the messaging queueing system. TODO
 * single-threaded?
 */
public class EndToEnd {


    // this security manager is used to verify that when we stop the middleware
    // actually system exit is called without exiting the test. It's based on the
    // idea taken from here: http://www.coderanch.com/t/540793/Testing/test-System-exit
    private class NoExitSecurityManager extends SecurityManager {

        private boolean throwException;

        public NoExitSecurityManager(boolean throwException) {
            this.throwException = throwException;
        }

        @Override
        public void checkPermission(Permission perm)    {
        }

        @Override
        public void checkPermission(Permission perm, Object context)    {
        }

        @Override
        public void checkExit(int status) {
            super.checkExit(status);

            if (throwException) {
                throw new RuntimeException("exit was called with status: " + status);
            }
        }
    }

    private ReadConfiguration mockMiddlewareConfiguration(String databaseHost, String databasePortNumber, String databaseName,
                                                          String databaseUsername, String databasePassword,
                                                          String threadPoolSize, String connectionPoolSize,
                                                          String dataSourceName, String middlewarePortNumber) {
        ReadConfiguration mockedConfiguration = mock(ReadConfiguration.class);

        when(mockedConfiguration.getProperty("databaseHost")).thenReturn(databaseHost);
        when(mockedConfiguration.getProperty("databasePortNumber")).thenReturn(databasePortNumber);
        when(mockedConfiguration.getProperty("databaseName")).thenReturn(databaseName);
        when(mockedConfiguration.getProperty("databaseUsername")).thenReturn(databaseUsername);
        when(mockedConfiguration.getProperty("databasePassword")).thenReturn(databasePassword);

        when(mockedConfiguration.getProperty("threadPoolSize")).thenReturn(threadPoolSize);
        when(mockedConfiguration.getProperty("connectionPoolSize")).thenReturn(connectionPoolSize);
        when(mockedConfiguration.getProperty("dataSourceName")).thenReturn(dataSourceName);
        when(mockedConfiguration.getProperty("middlewarePortNumber")).thenReturn(middlewarePortNumber);

        return mockedConfiguration;
    }

    private ReadConfiguration mockClientConfiguration(String middlewareHost, String middlewarePortNumber, String numberOfClients, String totalClients,
                                                      String startingId, String runningTimeInSeconds) {
        ReadConfiguration mockedConfiguration = mock(ReadConfiguration.class);

        when(mockedConfiguration.getProperty("middlewareHost")).thenReturn(middlewareHost);
        when(mockedConfiguration.getProperty("middlewarePortNumber")).thenReturn(middlewarePortNumber);

        when(mockedConfiguration.getProperty("numberOfClients")).thenReturn(numberOfClients);
        when(mockedConfiguration.getProperty("totalClients")).thenReturn(totalClients);
        when(mockedConfiguration.getProperty("startingId")).thenReturn(startingId);

        when(mockedConfiguration.getProperty("runningTimeInSeconds")).thenReturn(runningTimeInSeconds);

        return mockedConfiguration;
    }


    @Test(groups = END_TO_END, description = "This test creates 2 middlewares running locally on two different" +
            "ports and then creates 4 clients, 2 connected to each middleware that all of them 'talk' with each other." +
            "At the end it's checked that the requests sent from the clients were actually received by" +
            "the middlewares and that the responses sent from the middlewares were received by the clients.")
    public void testEndToEnd() throws InterruptedException, IOException, SQLException, ClassNotFoundException {

        /*  The following figure shows how clients are connected to the MW and to the database.

            Client(id = 1)  Client(id = 2)                  Client(id = 3)  Client(id = 4)
                   \              /                                 \           /
                    \            /                                   \         /
                     \          /                                     \       /
                      \        /                                       \     /
                       \      /                                         \   /
                      ----------                                      ----------
                      |  MW1   |                                      |  MW 2  |
                      ----------                                      ----------
                           |                                              |
                           |                                              |
                             --------------> Database <------------------

         */


        // initialize database
        final int totalClients = 4;
        int numberOfQueues = 1;
        InitializeDatabase.initializeDatabaseWithClientsAndQueues(HOST, PORT_NUMBER, DATABASE_NAME, USERNAME, PASSWORD,
                Connection.TRANSACTION_READ_COMMITTED, new String[]{}, totalClients, numberOfQueues);

        final ReadConfiguration[] middlewareConfigurations = {
                mockMiddlewareConfiguration(HOST, String.valueOf(PORT_NUMBER), DATABASE_NAME, USERNAME, PASSWORD,
                        "10", "10",  "middleware1", "6789"),

                mockMiddlewareConfiguration(HOST, String.valueOf(PORT_NUMBER), DATABASE_NAME, USERNAME, PASSWORD,
                        "10", "10",  "middleware2", "6790"),
        };


        // setup threads that are gong to use the middleware
        int numberOfMiddlewares = 2;
        final Middleware[] middleware = new Middleware[numberOfMiddlewares];
        Runnable[] middlewareRunnables = new Runnable[numberOfMiddlewares];
        Thread[] middlewareThreads = new Thread[numberOfMiddlewares];

        // Since many threads are going to be reading this value to know when
        // the middlewares instances were actually initialized we use AtomicBoolean.
        // This was done because you cannot have volatile array elements.
        final AtomicBoolean[] middlewaresInitialized = new AtomicBoolean[numberOfMiddlewares];

        for (int i = 0; i < numberOfMiddlewares; ++i) {
            final int finalI = i;
            middlewaresInitialized[i] = new AtomicBoolean(false);
            middlewareRunnables[i] = new Runnable() {
                @Override
                public void run() {
                    middleware[finalI] = new Middleware(middlewareConfigurations[finalI]);
                    middlewaresInitialized[finalI].set(true);
                    middleware[finalI].start(true);
                }
            };
            middlewareThreads[i] = new Thread(middlewareRunnables[i]);
        }

        // start middlewares
        for (int i = 0; i < numberOfMiddlewares; ++i) {
            middlewareThreads[i].start();
            while (!middlewaresInitialized[i].get());
            while (!middleware[i].hasStarted());
        }


        // create two clients connected to first middleware (port 6789)
        final int numberOfClientInstances = 2;
        final Client[] clients = new Client[numberOfClientInstances];
        Runnable[] clientRunnables = new Runnable[numberOfClientInstances];
        Thread[] clientThreads = new Thread[numberOfClientInstances];
        final AtomicBoolean[] clientsInitialized = new AtomicBoolean[numberOfClientInstances];


        int clientsPerInstance = totalClients / numberOfClientInstances;

        final ReadConfiguration[] clientConfigurations = {
                mockClientConfiguration("localhost", "6789", String.valueOf(clientsPerInstance), String.valueOf(totalClients), "1", "20"),
                mockClientConfiguration("localhost", "6790", String.valueOf(clientsPerInstance), String.valueOf(totalClients), "3", "20")};


        for (int i = 0; i < numberOfClientInstances; ++i) {
            final int finalI = i;
            clientsInitialized[i] = new AtomicBoolean(false);
            clientRunnables[i] = new Runnable() {
                @Override
                public void run() {
                    clients[finalI] = new Client(clientConfigurations[finalI]);
                    clientsInitialized[finalI].set(true);
                    clients[finalI].start(true);
                }
            };
        }

        for (int i = 0; i < numberOfClientInstances; ++i) {
            clientThreads[i] = new Thread(clientRunnables[i]);
            clientThreads[i].start();
            while (!clientsInitialized[i].get());
        }

        // wait till clients finish
        for (int i = 0; i < numberOfClientInstances; ++i) {
            clientThreads[i].join();
        }


        // verify results
        for (int i = 0; i < numberOfMiddlewares; ++i) {
            List<Request> receivedRequests = middleware[i].getAllRequests();
            List<Response> sentResponses = middleware[i].getAllResponses();

            // gather client1 and client2 sent requests & received responses
            List<Request> sentRequests = clients[i].getAllSentRequests();
            List<Response> receivedResponses = clients[i].getAllReceivedResponses();

            // compare received requests by the MW with sent requests from the clients
            assertFalse(receivedRequests.retainAll(sentRequests));

            // compare sent responses from the MW with received responses from the clients
            assertFalse(sentResponses.retainAll(receivedResponses));
        }

        // stop middlewares: middlewares exit after gracefully closing all
        // the middleware threads, so check that they actually exit as well
        System.setSecurityManager(new NoExitSecurityManager(true));

        for (int i = 0; i < numberOfMiddlewares; ++i) {
            try {
                middleware[i].stop();
            } catch (RuntimeException re) {
                assertEquals(re.getMessage(), "exit was called with status: 0");
            }
        }

        // change back to default security manager so no exception is thrown when this
        // test finishes
        System.setSecurityManager(new NoExitSecurityManager(false));
    }

}
