package testApp;

import app.TCPClient;

public class TORrentClient1 {

    public static void main(String[] args) {

        new Thread(() -> {
            try {
                TCPClient client1 = new TCPClient(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

    }

}
