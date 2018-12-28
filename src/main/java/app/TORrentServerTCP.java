package app;

import app.server.TCPServer;

public class TORrentServerTCP {

    public static void main(String[] args) {
        TCPServer server = new TCPServer();
        server.start();
    }
}
