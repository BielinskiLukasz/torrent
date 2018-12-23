package testApp;

import app.TCPClient;
import app.TCPServer;

public class TORrent {

    public static void main(String[] args) {

        TCPServer server = new TCPServer();
        TCPClient client1 = new TCPClient(1);
        TCPClient client2 = new TCPClient(2);
        TCPClient client3 = new TCPClient(3);

        server.run();
        client1.run();
        client2.run();
        client3.run();

    }

}
