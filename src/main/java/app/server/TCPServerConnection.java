package app.server;

import app.Utils.Config;
import app.Utils.Logger;

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
            System.out.println("TCPServerConnection - client connection " + e);
            e.printStackTrace();
        }

        while (true) {
            String clientSentence = null;
            try {
                clientSentence = inFromClient.readLine();
            } catch (IOException e) {
                System.out.println("TCPServerConnection - read line " + e);
                e.printStackTrace();
            }

            if (clientSentence != null) {
                TCPServerAction.perform(server, connectionSocket, clientSentence);
            }

            try { // TODO sleep
                sleep(Config.MILLISECONDS_OF_CONNECTION_LISTENER_WAITING);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
