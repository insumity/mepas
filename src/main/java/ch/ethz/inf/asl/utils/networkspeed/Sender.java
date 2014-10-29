package ch.ethz.inf.asl.utils.networkspeed;

import ch.ethz.inf.asl.common.request.ReceiveMessageRequest;
import ch.ethz.inf.asl.common.request.Request;
import ch.ethz.inf.asl.common.response.ReceiveMessageResponse;
import ch.ethz.inf.asl.common.response.Response;
import ch.ethz.inf.asl.utils.Helper;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;

public class Sender {

    private static class SenderThread implements Runnable {

        private Socket socket;
        private DataInputStream dataInputStream;
        private DataOutputStream dataOutputStream;
        private int numberOfSends = 0;
        private long sumOfTimes = 0;

        public SenderThread(Socket socket, int numberOfSends) throws IOException {
            this.socket = socket;
            this.dataInputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream(), 64));
            this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
            this.numberOfSends = numberOfSends;
        }

        public long getTimes() {
            return sumOfTimes;
        }

        private void sendRequest(Request request) throws IOException {
            byte[] data = Helper.serialize(request);
            dataOutputStream.write(data);
            dataOutputStream.flush();
        }

        private Response receiveResponse() throws IOException, ClassNotFoundException {
            int length = dataInputStream.readInt();
            byte[] data = new byte[length];
            byte[] lengthToByteArray = ByteBuffer.allocate(4).putInt(length).array();
            dataInputStream.readFully(data);
            byte[] concatenated = Helper.concatenate(lengthToByteArray, data);
            return (Response) Helper.deserialize(concatenated);
        }

        @Override
        public void run() {

            for (int i = 0; i < numberOfSends; ++i) {

                Request request = new ReceiveMessageRequest(3, 5, 1, false);
                try {
                    long startingTime = System.currentTimeMillis();
                    sendRequest(request);
                    Response response = receiveResponse();
                    sumOfTimes += (System.currentTimeMillis() - startingTime);

                    if (!response.isSuccessful()) {
                        throw new RuntimeException("Something was wrong!");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        int numberOfThreads = Integer.valueOf(args[0]);
        int numberOfSends = Integer.valueOf(args[1]);
        Thread[] threads = new Thread[numberOfThreads];
        SenderThread[] senderRunnables = new SenderThread[numberOfThreads];

        for (int i = 0; i < numberOfThreads; ++i) {
            Socket socket = new Socket(args[2], 6789);
            senderRunnables[i] = new SenderThread(socket, numberOfSends);
            threads[i] = new Thread(senderRunnables[i]);
        }

        for (int i = 0; i < numberOfThreads; ++i) {
            threads[i].start();
        }

        for (int i = 0; i < numberOfThreads; ++i) {
            threads[i].join();
        }


        long sumOfAllTimes = 0;
        for (int i = 0; i < numberOfThreads; ++i) {
            sumOfAllTimes += senderRunnables[i].getTimes();
        }

        System.out.println(sumOfAllTimes / ((double) (numberOfSends * numberOfThreads)));
    }
}
