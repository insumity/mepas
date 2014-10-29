package ch.ethz.inf.asl.utils.networkspeed;

import ch.ethz.inf.asl.common.Message;
import ch.ethz.inf.asl.common.response.ReceiveMessageResponse;
import ch.ethz.inf.asl.common.response.Response;
import ch.ethz.inf.asl.utils.Helper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Receiver {

    private static class ReceivingThread implements Runnable {

        private Socket socket;
        private DataInputStream dataInputStream;
        private DataOutputStream dataOutputStream;
        private byte[] dataToSendBack;

        public ReceivingThread(Socket socket) throws IOException {
            this.socket = socket;
            this.dataInputStream = new DataInputStream(socket.getInputStream());
            this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
            Date date = new Date();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String formattedDate = simpleDateFormat.format(date);

            Timestamp timeStamp = Timestamp.valueOf(formattedDate);
            Message message = new Message(1, 3, 4, timeStamp, "this is content and it's" +
                    "really good!");
            Response response = new ReceiveMessageResponse(message);
            dataToSendBack = Helper.serialize(response);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    int length = dataInputStream.readInt();
                    byte[] data = new byte[length];
                    dataInputStream.readFully(data);

                    dataOutputStream.write(dataToSendBack);
                    dataOutputStream.flush();
                } catch (IOException e) {
                    System.err.println("Probably(?) the socket got closed");
                    e.printStackTrace();
                    return;
                }
            }
        }
    }


    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(6789);

        while (true) {
            new Thread(new ReceivingThread(serverSocket.accept())).start();
        }
    }
}
