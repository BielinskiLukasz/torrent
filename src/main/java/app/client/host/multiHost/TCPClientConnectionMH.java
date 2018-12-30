package app.client.host.multiHost;

import app.client.host.ClientCommand;
import app.config.Config;
import app.utils.ConnectionUtils;
import app.utils.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPClientConnectionMH extends Thread {

    private TCPClientMH client;

    TCPClientConnectionMH(TCPClientMH tcpClientMH) {
        this.client = tcpClientMH;
    }

    public void run() {
        Logger.clientDebugLog("TCPClientConnectionMH: run");

        ServerSocket hostServerSocket = null;
        try {
            hostServerSocket = new ServerSocket(Config.PORT_NR + client.getClientNumber());
            connectWithServer();
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            Socket clientSocket;
            BufferedReader inFromServer;
            String clientSentence;

            try {
                clientSocket = hostServerSocket.accept();
                if (!hostServerSocket.isClosed()) {
                    inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    clientSentence = inFromServer.readLine();

                    TCPClientActionMH.perform(client.getClientNumber(), clientSocket, clientSentence);
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

    private void connectWithServer() {
        Logger.clientDebugLog("TCPClientConnectionMH: fire connectWithServer");

        Socket serverSocket = ConnectionUtils.createSocket(Config.HOST_IP, Config.PORT_NR);

        String helloMessage = ClientCommand.CONNECT + Config.SPLITS_CHAR + client.getClientNumber() +
                Config.SPLITS_CHAR + "Hello, I'm client " + client.getClientNumber();
        TCPClientActionMH.perform(client.getClientNumber(), serverSocket, helloMessage);

        ConnectionUtils.closeSocket(serverSocket);

        Logger.clientDebugLog("TCPClientConnectionMH: connectWithServer successfully");
    }
}
