package testApp;

import app.TCPClient;

public class TORrentClient1 {

    public static void main(String[] args) {

        TCPClient client = new TCPClient(1);
        client.run();

    }

}
