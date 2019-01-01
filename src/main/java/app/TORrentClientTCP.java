package app;

import app.client.host.multiHost.TCPClientMH;

class TORrentClientTCP {

    public static void main(String[] args) {
        TCPClientMH client = new TCPClientMH(Integer.parseInt(args[0]));
        client.start();
    }
}
