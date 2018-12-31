package app.server;

import app.config.Config;
import app.utils.ExceptionHandler;
import app.utils.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class TCPServerConnection extends Thread {

    private TCPServer server;
    private Socket connectionSocket;

    TCPServerConnection(TCPServer tcpServer, Socket connectionSocket) {
        this.server = tcpServer;
        this.connectionSocket = connectionSocket;

        Logger.serverDebugLog("TCPServerConnection: create connection");
    }

    public void run() {
        Logger.serverDebugLog("TCPServerConnection: run");

        BufferedReader inFromClient = null;
        try {
            inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        } catch (IOException e) {
            ExceptionHandler.handle(e);
        }

        while (true) {
            String clientSentence = null;
            try {
                clientSentence = inFromClient.readLine();
            } catch (IOException e) {
                ExceptionHandler.handle(e);
            }

            if (clientSentence != null) {
                TCPServerAction.perform(server, connectionSocket, clientSentence);
            }

            try { // TODO sleep
                sleep(Config.MILLISECONDS_OF_CONNECTION_LISTENER_WAITING);
            } catch (InterruptedException e) {
                ExceptionHandler.handle(e);
            }
        }
    }
}
