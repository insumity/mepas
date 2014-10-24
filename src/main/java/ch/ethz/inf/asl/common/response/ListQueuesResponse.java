package ch.ethz.inf.asl.common.response;

import java.util.Arrays;
import java.util.Objects;

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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ListQueuesResponse) {
            ListQueuesResponse other = (ListQueuesResponse) obj;
            return super.equals(other) && Arrays.equals(this.queueIds, other.queueIds);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), Objects.hash(queueIds));
    }
}
