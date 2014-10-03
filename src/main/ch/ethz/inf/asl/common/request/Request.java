package ch.ethz.inf.asl.common.request;

import ch.ethz.inf.asl.common.MessagingProtocol;
import ch.ethz.inf.asl.common.response.ReadMessageResponse;
import ch.ethz.inf.asl.common.response.Response;

import java.io.Serializable;

public abstract class Request implements Serializable {

    private int requestorId;

    public Request(int requestorId) {
        this.requestorId = requestorId;
    }

    public int getRequestorId() {
        return requestorId;
    }

    public abstract <R extends Response> R execute(MessagingProtocol protocol);

}
