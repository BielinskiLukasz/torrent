package app.client.host.host2host;

import app.Utils.Logger;
import app.client.console.host2host.TCPClientAppH2H;

public class TCPClientH2H extends Thread {

    private int clientNumber;

    public TCPClientH2H(int clientNumber) {
        this.clientNumber = clientNumber;

        Logger.clientDebugLog("TCPClientMH: create client");
    }

    public void run() {
        Logger.clientDebugLog("TCPClientMH - run");

        TCPClientAppH2H app = new TCPClientAppH2H(this);
        TCPClientConnectionH2H connection = new TCPClientConnectionH2H(this);
        app.start();
        connection.start();
    }

    public int getClientNumber() {
        return clientNumber;
    }
}