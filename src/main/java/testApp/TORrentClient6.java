package testApp;

import app.client.TCPClient;

public class TORrentClient6 {

    public static void main(String[] args) {

        TCPClient client = new TCPClient(6);
        client.start();

    }

}
