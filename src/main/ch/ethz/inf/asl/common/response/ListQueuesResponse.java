package ch.ethz.inf.asl.common.response;

import java.util.Arrays;

public class ListQueuesResponse extends Response {

    private int[] queueIds;

    public ListQueuesResponse(int[] queues) {
        queueIds = queues;
    }

    public int[] getQueues() {
        return queueIds;
    }

    @Override
    public String toString() {
        return "(LIST_QUEUES: " + Arrays.toString(queueIds) + ")";
    }
}
