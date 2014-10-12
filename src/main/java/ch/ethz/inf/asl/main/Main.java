package ch.ethz.inf.asl.main;

import ch.ethz.inf.asl.client.Client;
import ch.ethz.inf.asl.middleware.Middleware;

public class Main {

    public static void main(String[] args) {

        String type = args[0];
        if (type.equals("middleware")) {
            new Middleware();
        }
        else if (type.equals("client")) {
            new Client();
        }
        else {
            throw new IllegalArgumentException("What do you think you are doing?");
        }
    }
}
