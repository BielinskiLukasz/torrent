package testApp;

import app.client.TCPClient;

public class TORrentClient5 {

    public static void main(String[] args) {

        TCPClient client = new TCPClient(5);
        client.start();

    }

}
