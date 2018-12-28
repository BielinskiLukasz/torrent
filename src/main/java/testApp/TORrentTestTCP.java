package testApp;

import app.client.TCPClient;
import app.server.TCPServer;

public class TORrentTestTCP {

    public static void main(String[] args) {
        TCPServer server = new TCPServer();
        TCPClient client1 = new TCPClient(1);
        TCPClient client2 = new TCPClient(2);
        TCPClient client3 = new TCPClient(3);
        TCPClient client4 = new TCPClient(4);
        TCPClient client5 = new TCPClient(5);
        TCPClient client6 = new TCPClient(6);

        server.start();
        client1.start();
        client2.start();
        client3.start();
        client4.start();
        client5.start();
        client6.start();
    }
}
