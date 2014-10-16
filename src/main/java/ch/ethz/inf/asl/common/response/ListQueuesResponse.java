package ch.ethz.inf.asl.common.response;

import java.util.Arrays;

import static ch.ethz.inf.asl.utils.Verifier.notNull;

public class ListQueuesResponse extends Response {

    private int[] queueIds;

    // needed for creating failedResponse in Response class
    protected ListQueuesResponse() {}

    public ListQueuesResponse(int[] queues) {
        notNull(queues, "Given queues cannot be null");
        queueIds = queues;
    }

    public int[] getQueues() {
        return queueIds;
    }

    @Override
    public String toString() {
        if (!isSuccessful()) {
            return String.format("(LIST_QUEUES FAILED: %s)", getFailedMessage());
        }

        return String.format("(LIST_QUEUES SUCCESS: " + Arrays.toString(queueIds));
    }
}
