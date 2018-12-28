package testApp;

import app.client.TCPClient;

public class TORrentClient4 {

    public static void main(String[] args) {

        TCPClient client = new TCPClient(4);
        client.start();

    }

}
