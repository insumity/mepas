package ch.ethz.inf.asl.main;

import ch.ethz.inf.asl.client.Client;
import ch.ethz.inf.asl.utils.ConfigurationReader;
import ch.ethz.inf.asl.middleware.Middleware;
import ch.ethz.inf.asl.utils.networkspeed.Receiver;
import ch.ethz.inf.asl.utils.networkspeed.Sender;

import java.io.IOException;

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

//        if (args.length != EXPECTED_ARGUMENTS) {
//            printErrorMessageAndExit();
//        }
//
        String type = args[0]; // FIXME TODO
//        boolean isValidType = type.equals(MIDDLEWARE) || type.equals(CLIENT);
//        if (!isValidType) {
//            printErrorMessageAndExit();
//        }

        String configurationFilePath = null;
        if (type.equals(MIDDLEWARE) || type.equals(CLIENT)) {
            configurationFilePath = args[1];
        }

        if (type.equals(MIDDLEWARE)) {
            new Middleware(new ConfigurationReader(configurationFilePath)).start(false);
        } else if (type.equals(CLIENT)) {
            assert(type.equals(CLIENT));
            new Client(new ConfigurationReader(configurationFilePath)).start(false);
        } else if (type.equals("receiver")) {
            try {
                new Receiver().main(null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (type.equals("sender")) {
            try {
                new Sender().main(new String[] { args[1], args[2], args[3] });
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
