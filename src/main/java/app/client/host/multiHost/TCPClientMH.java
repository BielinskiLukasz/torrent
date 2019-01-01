package app.client.host.multiHost;

import app.client.console.multiHost.TCPConsoleConnectionMH;
import app.utils.Logger;

public class TCPClientMH extends Thread {

    private final int clientNumber;

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