package app;

import app.client.TCPClient;

public class TORrentClientTCP {

    public static void main(String[] args) {
        TCPClient client = new TCPClient(Integer.parseInt(args[0]));
        client.start();
    }
}
