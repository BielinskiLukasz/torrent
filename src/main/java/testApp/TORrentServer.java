package testApp;

import app.TCPServer;

public class TORrentServer {

    public static void main(String[] args) {

        new Thread(() -> {
            try {
                TCPServer server = new TCPServer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

    }

}
