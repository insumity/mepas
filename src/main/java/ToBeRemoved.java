public class ToBeRemoved {

    public static void main(String[] args) {

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (true);
            }
        });

        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
