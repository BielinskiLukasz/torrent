package testApp;

import app.client.host.multiHost.TCPClientMH;
import app.server.TCPServer;

public class TORrentTestTCP {

    public static void main(String[] args) {
        TCPServer server = new TCPServer();
        TCPClientMH client1 = new TCPClientMH(1);
        TCPClientMH client2 = new TCPClientMH(2);
        TCPClientMH client3 = new TCPClientMH(3);
        TCPClientMH client4 = new TCPClientMH(4);
        TCPClientMH client5 = new TCPClientMH(5);
        TCPClientMH client6 = new TCPClientMH(6);

        server.start();
        client1.start();
        client2.start();
        client3.start();
        client4.start();
        client5.start();
        client6.start();
    }
}
