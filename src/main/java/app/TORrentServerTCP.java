package app;

import app.server.TCPServer;

class TORrentServerTCP {

    public static void main(String[] args) {
        TCPServer server = new TCPServer();
        server.start();
    }
}
