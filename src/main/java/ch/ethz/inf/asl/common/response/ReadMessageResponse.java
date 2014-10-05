package ch.ethz.inf.asl.common.response;

import ch.ethz.inf.asl.common.Message;
import ch.ethz.inf.asl.common.request.CreateQueueRequest;
import ch.ethz.inf.asl.common.request.ReceiveMessageRequest;
import ch.ethz.inf.asl.utils.Optional;

// same as RECEIVE_MESSAGE! What's the difference bro?? FIXME TODO
public class ReadMessageResponse extends Response {
    public ReadMessageResponse(Optional<Message> message) {
    }
}