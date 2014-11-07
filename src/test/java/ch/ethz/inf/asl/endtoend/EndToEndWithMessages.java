package ch.ethz.inf.asl.endtoend;

import ch.ethz.inf.asl.client.ClientMessagingProtocolImpl;
import ch.ethz.inf.asl.utils.ConfigurationReader;
import ch.ethz.inf.asl.common.Message;
import ch.ethz.inf.asl.middleware.Middleware;
import ch.ethz.inf.asl.testutils.InitializeDatabase;
import ch.ethz.inf.asl.utils.Optional;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

import static ch.ethz.inf.asl.testutils.TestConstants.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.AssertJUnit.assertTrue;


public class EndToEndWithMessages {

    private static final String DATABASE_NAME = "endtoendtest";

    @Test(groups = END_TO_END)
    public void testSpecificMessagesAreBeingSentAndReceived() throws ClassNotFoundException, SQLException, InterruptedException, IOException {

        InitializeDatabase.initializeDatabaseWithClientsAndQueues(HOST, PORT_NUMBER, DATABASE_NAME, USERNAME, PASSWORD,
                new String[]{}, 2, 100);

        final ConfigurationReader middlewareConfiguration =
                ConfigurationMocker.mockMiddlewareConfiguration(HOST, String.valueOf(PORT_NUMBER), DATABASE_NAME, USERNAME,
                        PASSWORD, "10", "10",  "middleware1", "6790");


        final Middleware[] middleware = new Middleware[1];
        Runnable middlewareRunnable;
        Thread middlewareThread;

        // Since many threads are going to be reading this value to know when
        // the middlewares instances were actually initialized we use AtomicBoolean.
        // This was done because you cannot have volatile array elements.
        final AtomicBoolean[] middlewareInitialized = new AtomicBoolean[1];
        middlewareInitialized[0] = new AtomicBoolean(false);

        middlewareRunnable = new Runnable() {
            @Override
            public void run() {
                middleware[0] = new Middleware(middlewareConfiguration);
                middlewareInitialized[0].set(true);
                middleware[0].start(true);
            }
        };
        middlewareThread = new Thread(middlewareRunnable);

        middlewareThread.start();
        while (!middlewareInitialized[0].get());
        while (!middleware[0].hasStarted());


        Socket socket = new Socket("localhost", 6790);
        ClientMessagingProtocolImpl client1 = new ClientMessagingProtocolImpl(socket, 1, false);

        client1.sendMessage(19, "this is some content");
        client1.sendMessage(2, 78, "cool, isn't it?");
        client1.createQueue("some name");
        int[] queues = client1.listQueues();
        assertEquals(queues, new int[]{});
        socket.close();

        socket = new Socket("localhost", 6790);
        ClientMessagingProtocolImpl client2 = new ClientMessagingProtocolImpl(socket, 2, false);

        queues = client2.listQueues();
        assertEquals(queues, new int[] {19, 78});

        Optional<Message> message = client2.readMessage(78, false);
        assertTrue(message.isPresent());
        Message receivedMessage = message.get();
        assertEquals(receivedMessage.getQueueId(), 78);
        assertEquals(receivedMessage.getContent(), "cool, isn't it?");
        assertEquals(receivedMessage.getReceiverId(), 2);
        assertEquals(receivedMessage.getSenderId(), 1);

        message = client2.receiveMessage(78, false);
        assertTrue(message.isPresent());
        receivedMessage = message.get();
        assertEquals(receivedMessage.getQueueId(), 78);
        assertEquals(receivedMessage.getContent(), "cool, isn't it?");
        assertEquals(receivedMessage.getReceiverId(), 2);
        assertEquals(receivedMessage.getSenderId(), 1);

        int unknownSender = 13434;
        message = client2.receiveMessage(unknownSender, 19, false);
        assertFalse(message.isPresent());

        message = client2.receiveMessage(1, 19, true);
        assertTrue(message.isPresent());
        receivedMessage = message.get();
        assertEquals(receivedMessage.getQueueId(), 19);
        assertEquals(receivedMessage.getContent(), "this is some content");
        assertFalse(receivedMessage.hasReceiver());
        assertEquals(receivedMessage.getSenderId(), 1);
    }
}
