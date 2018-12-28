package testApp;

import app.server.TCPServer;

public class TORrentServer {

    public static void main(String[] args) {

        TCPServer server = new TCPServer();
        server.start();

    }

}
