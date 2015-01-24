package ch.ethz.inf.asl.common.response;

import java.util.Objects;

public class SayGoodbyeResponse extends Response {

    @Override
    public String toString() {
        if (!isSuccessful()) {
            return String.format("(GOODBYE FAILED: %s)", getFailedMessage());
        }

        return "(GOODBYE SUCCESS)";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SayGoodbyeResponse) {
            SayGoodbyeResponse other = (SayGoodbyeResponse) obj;
            return super.equals(other);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }
}
