package testApp;

import app.TCPServer;

public class TORrentServer {

    public static void main(String[] args) {

        TCPServer server = new TCPServer();
        server.run();

    }

}
