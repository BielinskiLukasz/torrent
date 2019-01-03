package app;

import app.client.host.host2host.TCPClientH2H;

class TORrentHostTCP {

    public static void main(String[] args) {
        TCPClientH2H client = null;

        if (args.length == 2)
            client = new TCPClientH2H(Integer.parseInt(args[0], Integer.parseInt(args[1])));
        else if (args.length == 1)
            client = new TCPClientH2H(Integer.parseInt(args[0]));

        client.start();
    }
}
