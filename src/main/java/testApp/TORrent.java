package testApp;

import app.TCPClient;
import app.TCPServer;

public class TORrent {

    public static void main(String[] args) {

        TCPServer server = new TCPServer();
        TCPClient client1 = new TCPClient(1);
        TCPClient client2 = new TCPClient(2);
        TCPClient client3 = new TCPClient(3);

        server.start();
        client1.start();
        client2.start();
        client3.start();

    }

}
