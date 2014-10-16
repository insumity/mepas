package ch.ethz.inf.asl.common.response;

public class GoodbyeResponse extends Response {

    @Override
    public String toString() {
        if (!isSuccessful()) {
            return String.format("(GOODBYE FAILED: %s)", getFailedMessage());
        }

        return "(GOODBYE SUCCESS)";
    }
}
