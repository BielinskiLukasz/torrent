package testApp;

import app.client.TCPClient;

public class TORrentClient2 {

    public static void main(String[] args) {

        TCPClient client = new TCPClient(2);
        client.start();

    }

}
