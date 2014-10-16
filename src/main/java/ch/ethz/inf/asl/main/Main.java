package ch.ethz.inf.asl.main;

import ch.ethz.inf.asl.client.Client;
import ch.ethz.inf.asl.console.Manager;
import ch.ethz.inf.asl.middleware.Middleware;

import java.util.Arrays;

public class Main {

    public static void main(String[] args) {

        String type = args[0];
        if (type.equals("middleware")) {
            String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
            new Middleware(newArgs);
        }
        else if (type.equals("client")) {
            String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
            new Client(newArgs, false);
        }
        else if (type.equals("manager")) {
            new Manager();
        }
        else {
            System.out.println("Please check the provided arguments!");
        }
    }
}
