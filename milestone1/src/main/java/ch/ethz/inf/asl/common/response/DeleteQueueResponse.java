package ch.ethz.inf.asl.common.response;

import java.util.Objects;

public class DeleteQueueResponse extends Response {

        @Override
        public String toString() {
            if (!isSuccessful()) {
                return String.format("(DELETE_QUEUE FAILED: %s)", getFailedMessage());
            }

            return "(DELETE_QUEUE SUCCESS)";
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof DeleteQueueResponse) {
                DeleteQueueResponse other = (DeleteQueueResponse) obj;
                return super.equals(other);
            }

            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode());
        }
}
