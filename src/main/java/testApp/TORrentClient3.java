package testApp;

import app.TCPClient;

public class TORrentClient3 {

    public static void main(String[] args) {

        TCPClient client = new TCPClient(3);
        client.run();

    }

}
