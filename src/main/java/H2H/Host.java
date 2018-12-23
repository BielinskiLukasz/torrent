package H2H;

import app.TCPClient;
import app.TCPServer;

import java.io.IOException;

public class Host {

    TCPClient client;
    TCPServer server;

    public Host(int hostId) throws IOException {
        server = new TCPServer();
        client = new TCPClient(hostId);
    }

}
