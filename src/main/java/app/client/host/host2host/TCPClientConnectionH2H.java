package app.client.host.host2host;

import app.client.host.ClientCommand;
import app.config.Config;
import app.utils.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;

class TCPClientConnectionH2H extends Thread {

    private final TCPClientH2H client;

    TCPClientConnectionH2H(TCPClientH2H tcpClientH2h) {
        this.client = tcpClientH2h;
    }

    public void run() {
        Logger.clientDebugLog("TCPClientConnectionH2H: run");

        ServerSocket hostServerSocket = null;
        try {
            hostServerSocket = new ServerSocket(Config.PORT_NR + client.getClientNumber());
            if (client.getConnectedClientNumber() != Config.INT_SV) {
                connectWithHost(client.getClientNumber(), client.getConnectedClientNumber());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            Socket connectionSocket;
            BufferedReader inFromHost;
            String clientSentence;

            try {
                connectionSocket = Objects.requireNonNull(hostServerSocket).accept();
                if (!hostServerSocket.isClosed()) {
                    inFromHost = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                    clientSentence = inFromHost.readLine();

                    TCPClientActionH2H.perform(client, connectionSocket, clientSentence);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try { // TODO sleep
                sleep(Config.MILLISECONDS_OF_CONNECTION_LISTENER_WAITING);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void connectWithHost(int clientNumber, int connectedClientNumber) {
        Logger.clientDebugLog("TCPClientConnectionH2H: fire connectWithHost");

        Socket hostClientSocket = null;
        try {
            hostClientSocket = new Socket(Config.HOST_IP, Config.PORT_NR + connectedClientNumber);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String helloMessage = ClientCommand.CONNECT + Config.SPLITS_CHAR + client.getClientNumber() +
                Config.SPLITS_CHAR + "Hello, I'm client " + client.getClientNumber();
        TCPClientActionH2H.perform(client, hostClientSocket, helloMessage);

        try {
            Objects.requireNonNull(hostClientSocket).close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Logger.clientDebugLog("TCPClientConnectionH2H: connectWithHost successfully");
    }
}
