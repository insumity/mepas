package ch.ethz.inf.asl.main;

import ch.ethz.inf.asl.client.Client;
import ch.ethz.inf.asl.middleware.Middleware;

import java.util.Arrays;

public class Main {

    private static int EXPECTED_ARGUMENTS = 2;
    private static final String MIDDLEWARE = "middleware";
    private static final String CLIENT = "client";

    private static void printErrorMessageAndExit() {
        String ERROR_MESSAGE = "Please check the provided arguments! The should be of the " +
                "following format: (" + MIDDLEWARE + " | " + CLIENT + ") configurationFile\n" +
                "For example: \"" + MIDDLEWARE + " /dir/middleware.properties\"";
        System.err.println(ERROR_MESSAGE);

        System.exit(1);
    }

    public static void main(String[] args) {

        if (args.length != EXPECTED_ARGUMENTS) {
            printErrorMessageAndExit();
        }

        String type = args[0];
        boolean isValidType = type.equals(MIDDLEWARE) || type.equals(CLIENT);
        if (!isValidType) {
            printErrorMessageAndExit();
        }

        String[] newArguments = Arrays.copyOfRange(args, 1, args.length);

        if (type.equals(MIDDLEWARE)) {
            new Middleware(newArguments).start(false);
        } else {
            assert(type.equals(CLIENT));
            new Client(newArguments).start(false);
        }
    }
}
