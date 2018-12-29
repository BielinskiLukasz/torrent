package app.client.host.multihost;

import app.Utils.Logger;
import app.client.host.CommandClient;
import app.client.host.TCPClientAction;
import app.config.Config;

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
            System.out.println("TCPClientConnectionMH - client initiation " + e);
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

                    TCPClientAction.perform(client.getClientNumber(), connectionSocket, clientSentence);
                }
            } catch (IOException e) {
                System.out.println("TCPClientConnectionMH - read line from server " + e);
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

        Socket hostClientSocket = null;
        try {
            hostClientSocket = new Socket(Config.HOST_IP, Config.PORT_NR);
        } catch (IOException e) {
            System.out.println("TCPClientConnectionMH - creating a socket to initiate the connection " + e);
            e.printStackTrace();
        }

        String helloMessage = CommandClient.CONNECT + Config.SENTENCE_SPLITS_CHAR + client.getClientNumber() +
                Config.SENTENCE_SPLITS_CHAR + "Hello, I'm client " + client.getClientNumber();
        TCPClientAction.perform(client.getClientNumber(), hostClientSocket, helloMessage);

        try {
            hostClientSocket.close();
        } catch (IOException e) {
            System.out.println("TCPClientConnectionMH - close client socket " + e);
            e.printStackTrace();
        }

        Logger.clientDebugLog("TCPClientConnectionMH: connectWithServer successfully");
    }
}
