package app.client.host;

import app.Utils.Config;
import app.Utils.Logger;
import app.client.TCPClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPClientConnection extends Thread {

    private TCPClient client;

    public TCPClientConnection(TCPClient tcpClient) {
        this.client = tcpClient;
    }

    public void run() {
        Logger.clientDebugLog("TCPClientConnection: run");

        ServerSocket hostServerSocket = null;
        try {
            hostServerSocket = new ServerSocket(Config.PORT_NR + client.getClientNumber());
            connectWithServer();
        } catch (IOException e) {
            System.out.println("TCPClientConnection - client initiation " + e);
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
                System.out.println("TCPClientConnection - read line from server " + e);
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
        Logger.clientDebugLog("TCPClientConnection: fire connectWithServer");

        Socket hostClientSocket = null;
        try {
            hostClientSocket = new Socket(Config.HOST_IP, Config.PORT_NR);
        } catch (IOException e) {
            System.out.println("TCPClientConnection - creating a socket to initiate the connection " + e);
            e.printStackTrace();
        }

        String helloMessage = CommandClient.CONNECT + Config.SENTENCE_SPLITS_CHAR + client.getClientNumber() +
                Config.SENTENCE_SPLITS_CHAR + "Hello, I'm client " + client.getClientNumber();
        TCPClientAction.perform(client.getClientNumber(), hostClientSocket, helloMessage);

        try {
            hostClientSocket.close();
        } catch (IOException e) {
            System.out.println("TCPClientConnection - close client socket " + e);
            e.printStackTrace();
        }

        Logger.clientDebugLog("TCPClientConnection: connectWithServer successfully");
    }
}
