package app.client.host.multihost;

import app.Utils.Logger;
import app.client.console.multihost.TCPClientAppMH;

public class TCPClientMH extends Thread {

    private int clientNumber;

    public TCPClientMH(int clientNumber) {
        this.clientNumber = clientNumber;

        Logger.clientDebugLog("TCPClientMH: create client");
    }

    public void run() {
        Logger.clientDebugLog("TCPClientMH - run");

        TCPClientAppMH app = new TCPClientAppMH(this);
        TCPClientConnectionMH connection = new TCPClientConnectionMH(this);
        app.start();
        connection.start();
    }

    public int getClientNumber() {
        return clientNumber;
    }
}