package ch.ethz.inf.asl.endtoend;

import ch.ethz.inf.asl.client.ClientRunnable;
import ch.ethz.inf.asl.common.request.Request;
import ch.ethz.inf.asl.common.response.Response;
import ch.ethz.inf.asl.middleware.Middleware;
import ch.ethz.inf.asl.testutils.InitializeDatabase;
import org.testng.annotations.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static ch.ethz.inf.asl.testutils.TestConstants.*;

/**
 * End to end testing for the messaging queueing system. TODO
 * single-threaded?
 */
public class EndToEnd {


    @Test(groups = END_TO_END)
    public void testEndToEnd() throws InterruptedException, IOException, SQLException, ClassNotFoundException {

        // start the middleware
        InitializeDatabase.initialize(HOST, PORT_NUMBER, DATABASE_NAME, USERNAME, PASSWORD,
                Connection.TRANSACTION_READ_COMMITTED, new String[]{});


        final String[] middlewareArgs = {HOST, USERNAME, PASSWORD, DATABASE_NAME,
                String.valueOf(PORT_NUMBER), "10", "10", "6789"};


        final Middleware[] middleware = new Middleware[1];
        Runnable middlewareRunnable = new Runnable() {
            @Override
            public void run() {
                middleware[0] = new Middleware(middlewareArgs);
                middleware[0].start(true);
            }
        };

        Thread middlewareThread = new Thread(middlewareRunnable);
        middlewareThread.start();

        // TODO fixme wait for MW to start
        System.err.println("ola kala!");
        String s = "karolos";
        for (float i = 0; i < 100000; i++) {
            s += "sf";
        }

        System.err.println("ola kala!" + "DSf");

        ClientRunnable client1 = new ClientRunnable(20, 1, "localhost", 6789, 2, true);
        ClientRunnable client2 = new ClientRunnable(10, 2, "localhost", 6789, 2, true);

        Thread client1Thread = new Thread(client1);
        Thread client2Thread = new Thread(client2);

        client1Thread.start();
        client2Thread.start();

        System.err.println("Clients started!");
        client1Thread.join();
        client2Thread.join();
        System.err.println("Client's finished!");

        middleware[0].stop();
        List<Request> requests = middleware[0].getAllRequests();
        List<Response> responses = middleware[0].getAllResponses();
        System.err.println("MW: " + requests.size() + ":" + responses.size());

        List<Request> client1Requests = client1.getSentRequets();
        List<Response> client1Responses = client1.getReceivedResponse();

        List<Request> client2Requests = client2.getSentRequets();
        List<Response> client2Responses = client2.getReceivedResponse();


        System.err.println("client1: " + client1Requests.size() + ":" + client1Responses.size());
        System.err.println("client2: " + client2Requests.size() + ":" + client2Responses.size());

        client1Requests.addAll(client2Requests);
        client1Responses.addAll(client2Responses);

        // compare requests and responses
        // SAME requests sent by clients, and same requests received by the MW
        // same responses sent by MW, same responses received by Clients
        boolean result1 = client1Requests.retainAll(requests);
        boolean result2 = client1Responses.retainAll(responses);

        System.err.println("Result:" + result1 + ", result: "+ result2 + ", client1: " + client1Requests.size() + ":" + client1Responses.size());


        middlewareThread.join();
    }

}
