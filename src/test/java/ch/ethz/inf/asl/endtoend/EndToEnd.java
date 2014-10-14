package ch.ethz.inf.asl.endtoend;

import ch.ethz.inf.asl.common.Message;
import ch.ethz.inf.asl.middleware.Middleware;
import ch.ethz.inf.asl.utils.Optional;
import org.testng.annotations.Test;

import static ch.ethz.inf.asl.utils.TestConstants.END_TO_END;

/**
 * End to end testing for the messaging queueing system. TODO
 * single-threaded?
 */
public class EndToEnd {

//    @Test(groups = END_TO_END)
//    public void testEndToEnd() {
//
//        // start the middleware
//        new Middleware().start();
//
//        // start a client that sends a message
//        new Client(1).sendMessage(2, 1, "someData");
//        // verify that client sended the message
//        // verify db contains the message
//
//        Optional<Message> message = new Client(2).receiveMessage(1, 1);
//        // verify received message is the one sent by the user
// /   }
}
