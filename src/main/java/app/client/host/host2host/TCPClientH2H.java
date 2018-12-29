package app.client.host.host2host;

import app.client.console.host2host.TCPConsoleConnectionH2H;
import app.config.Config;
import app.utils.Logger;

public class TCPClientH2H extends Thread {

    private int clientNumber;
    private int connectedClientNumber;

    public TCPClientH2H(int clientNumber) {
        this.clientNumber = clientNumber;
        this.connectedClientNumber = Config.INT_SV;

        Logger.clientDebugLog("TCPClientH2H: create first host");
    }

    public TCPClientH2H(int clientNumber, int connectedClientNumber) {
        this.clientNumber = clientNumber;
        this.connectedClientNumber = connectedClientNumber;

        Logger.clientDebugLog("TCPClientH2H: create first host");
    }

    public void run() {
        Logger.clientDebugLog("TCPClientH2H - run");

        TCPConsoleConnectionH2H app = new TCPConsoleConnectionH2H(this);
        TCPClientConnectionH2H connection = new TCPClientConnectionH2H(this);
        app.start();
        connection.start();
    }

    public int getClientNumber() {
        return clientNumber;
    }

    public int getConnectedClientNumber() {
        return connectedClientNumber;
    }

    void setConnectedClientNumber(int connectedClientNumber) {
        this.connectedClientNumber = connectedClientNumber;
    }
}