package ch.ethz.inf.asl.client.nio;

import ch.ethz.inf.asl.common.Message;
import ch.ethz.inf.asl.exceptions.MessageProtocolException;
import ch.ethz.inf.asl.utils.Optional;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;

public class NIOClient {

    // duplicated method FIXME
    private static String stringOf(int length) {
        String a = "";

        for (int i = 0; i < length; ++i) {
            a += "|";
        }
        return a;
    }

    public static void main(String[] args) throws IOException {

        int userId = 2;
        int otherUserId = 3;
        String hostName = "localhost";
        int portNumber = 3000;
        String content = stringOf(200);

        SocketAddress address = new InetSocketAddress(hostName, portNumber);
        SocketChannel client = SocketChannel.open(address);
        client.configureBlocking(false);

        // wait until the connection is established
        while (!client.finishConnect());

        NIOClientMessagingProtocolImpl protocol =
                new NIOClientMessagingProtocolImpl(userId, client);

        System.err.println("after protocol initialization");
        int result = protocol.createQueue("mepasCool");
        System.out.println("Queue created with queue id: " + result);

        boolean send = true;
        while (true) {

            try {
                if (send) {
                    protocol.sendMessage(otherUserId, 1, content);
                    System.err.println("Send message");
                    send = !send;
                } else {
                    Optional<Message> message = protocol.receiveMessage(1, true);
                    if (message.isPresent()) {
                        System.err.println("Received message: " + message.get());
                    }
                    else {
                        System.err.println("Received message: NONE");
                    }
                    send = !send;
                }
            } catch (MessageProtocolException e) {
                e.printStackTrace();
                break;
            }
        }

    }
}