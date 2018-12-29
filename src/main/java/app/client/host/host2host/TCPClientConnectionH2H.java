package app.client.host.host2host;

import app.Utils.Logger;
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

                    TCPClientActionH2H.perform(client.getClientNumber(), connectionSocket, clientSentence);
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
}
