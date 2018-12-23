package testApp;

import app.TCPClient;

public class TORrentClient3 {

    public static void main(String[] args) {

        new Thread(() -> {
            try {
                TCPClient client3 = new TCPClient(3);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

    }

}
