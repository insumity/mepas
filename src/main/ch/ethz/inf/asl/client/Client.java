package ch.ethz.inf.asl.client;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

    public static void main(String[] args) {
        String hostName = "localhost";

        try {
            Socket socket = new Socket(hostName, 6789);

            new ClientMessagingProtocolImpl(34, null, null);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream();
            oos.writeObject(new Object());

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            out.write("what's up coolness!\n");
            out.flush();
            out.close();
            in.close();;
            socket.close();;

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /*
    connect to MW based on configuration
    send messages
    ClientProtocol protocol = new ...Protocol(MW_IP_ADDRESS, ...);

    while(true) {

        protocol.sendMessage(); // actually opens the connection with MW and sends
        a message

        Message msg = protocol.receiveMessage();

    }
     */
}
