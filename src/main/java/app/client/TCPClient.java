package app.client;

import app.Utils.Logger;
import app.client.console.TCPClientApp;
import app.client.host.TCPClientConnection;

public class TCPClient extends Thread {

    private int clientNumber;

    public TCPClient(int clientNumber) {
        this.clientNumber = clientNumber;

        Logger.clientDebugLog("TCPClient: create client");
    }

    public void run() {
        Logger.clientDebugLog("TCPClient - run");

        TCPClientApp app = new TCPClientApp(this);
        TCPClientConnection connection = new TCPClientConnection(this);
        app.start();
        connection.start();
    }

    public int getClientNumber() {
        return clientNumber;
    }
}