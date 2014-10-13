package ch.ethz.inf.asl.main;

import ch.ethz.inf.asl.client.Client;
import ch.ethz.inf.asl.middleware.Middleware;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) throws IOException, SQLException, InterruptedException {

        String type = args[0];
        if (type.equals("middleware")) {
            String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
            Middleware.main(newArgs);
        }
        else if (type.equals("client")) {
            String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
            Client.main(newArgs);
        }
        else {
            throw new IllegalArgumentException("What do you think you are doing?");
        }
    }
}
