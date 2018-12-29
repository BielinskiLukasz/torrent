package app.client.host.host2host;

import app.Utils.Logger;
import app.client.host.CommandClient;
import app.client.host.multihost.TCPClientActionMH;
import app.config.Config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPClientConnectionH2H extends Thread {

    private TCPClientH2H client;

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
            System.out.println("TCPClientConnectionH2H - client initiation " + e);
            e.printStackTrace();
        }

        while (true) {
            Socket connectionSocket;
            BufferedReader inFromServer;
            String clientSentence;

            try {
                connectionSocket = hostServerSocket.accept();
                if (!hostServerSocket.isClosed()) {
                    inFromServer = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                    clientSentence = inFromServer.readLine();

                    TCPClientActionH2H.perform(client, connectionSocket, clientSentence);
                }
            } catch (IOException e) {
                System.out.println("TCPClientConnectionH2H - read line from server " + e);
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
            System.out.println("TCPClientConnectionH2H - creating a socket to initiate the connection " + e);
            e.printStackTrace();
        }

        String helloMessage = CommandClient.CONNECT + Config.SENTENCE_SPLITS_CHAR + client.getClientNumber() +
                Config.SENTENCE_SPLITS_CHAR + "Hello, I'm client " + client.getClientNumber();
        TCPClientActionMH.perform(client.getClientNumber(), hostClientSocket, helloMessage);

        try {
            hostClientSocket.close();
        } catch (IOException e) {
            System.out.println("TCPClientConnectionH2H - close client socket " + e);
            e.printStackTrace();
        }

        Logger.clientDebugLog("TCPClientConnectionH2H: connectWithHost successfully");
    }
}
