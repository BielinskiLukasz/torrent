package app.client.host.multiHost;

import app.client.host.ClientCommand;
import app.config.Config;
import app.utils.ExceptionHandler;
import app.utils.Logger;
import app.utils.connectionUtils.TCPConnectionUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;

class TCPClientConnectionMH extends Thread {

    private final TCPClientMH client;

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
            ExceptionHandler.handle(e);
        }

        while (true) {
            Socket clientSocket;
            BufferedReader inFromServer;
            String clientSentence;

            try {
                clientSocket = Objects.requireNonNull(hostServerSocket).accept();
                if (!hostServerSocket.isClosed()) {
                    inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    clientSentence = inFromServer.readLine();

                    TCPClientConnectionActionMH.perform(client.getClientNumber(), clientSocket, clientSentence);
                }
            } catch (IOException e) {
                ExceptionHandler.handle(e);
            }

            try { // TODO sleep
                sleep(Config.MILLISECONDS_OF_CONNECTION_LISTENER_WAITING);
            } catch (InterruptedException e) {
                ExceptionHandler.handle(e);
            }
        }
    }

    private void connectWithServer() {
        Logger.clientDebugLog("TCPClientConnectionMH: fire connectWithServer");

        Socket serverSocket = TCPConnectionUtils.createSocket(Config.HOST_IP, Config.PORT_NR);

        String helloMessage = ClientCommand.CONNECT + Config.SPLITS_CHAR + client.getClientNumber() +
                Config.SPLITS_CHAR + "Hello, I'm client " + client.getClientNumber();
        TCPClientConnectionActionMH.perform(client.getClientNumber(), serverSocket, helloMessage);

        TCPConnectionUtils.closeSocket(serverSocket);

        Logger.clientDebugLog("TCPClientConnectionMH: connectWithServer successfully");
    }
}
