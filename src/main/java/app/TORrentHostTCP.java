package app;

import app.client.host.host2host.TCPClientH2H;

class TORrentHostTCP {

    public static void main(String[] args) {
        TCPClientH2H client = new TCPClientH2H(Integer.parseInt(args[0]));
        client.start();
    }
}
