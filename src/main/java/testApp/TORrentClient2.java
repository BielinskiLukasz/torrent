package testApp;

import app.TCPClient;

public class TORrentClient2 {

    public static void main(String[] args) {

        new Thread(() -> {
            try {
                TCPClient client2 = new TCPClient(2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

    }

}
