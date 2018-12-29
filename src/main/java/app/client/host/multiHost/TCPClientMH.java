package app.client.host.multiHost;

import app.Utils.Logger;
import app.client.console.multiHost.TCPConsoleConnectionMH;

public class TCPClientMH extends Thread {

    private int clientNumber;

    public TCPClientMH(int clientNumber) {
        this.clientNumber = clientNumber;

        Logger.clientDebugLog("TCPClientMH: create client");
    }

    public void run() {
        Logger.clientDebugLog("TCPClientMH - run");

        TCPConsoleConnectionMH app = new TCPConsoleConnectionMH(this);
        TCPClientConnectionMH connection = new TCPClientConnectionMH(this);
        app.start();
        connection.start();
    }

    public int getClientNumber() {
        return clientNumber;
    }
}