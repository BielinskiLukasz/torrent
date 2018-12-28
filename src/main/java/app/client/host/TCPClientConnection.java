package app.client.host;

import app.Utils.Config;
import app.client.TCPClient;
import app.client.console.CommandApp;

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
        ServerSocket hostServerSocket = null;

        System.out.println("TCPClientConnection: run"); // TODO debug log <-- not visible

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
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void connectWithServer() {
        Socket hostClientSocket = null;

        System.out.println("TCPClientConnection: connectWithServer"); // TODO debug log

        try {
            hostClientSocket = new Socket(Config.HOST_IP, Config.PORT_NR);
        } catch (IOException e) {
            System.out.println("TCPClientConnection - creating a socket to initiate the connection " + e);
            e.printStackTrace();
        }

        // TODO implement adding user, files on server side !!!
        String helloMessage = CommandApp.CONNECT + Config.SENTENCE_SPLITS_CHAR + "Hello, I'm client " + client.getClientNumber();
        TCPClientAction.perform(client.getClientNumber(), hostClientSocket, helloMessage);

        try {
            hostClientSocket.close();
        } catch (IOException e) {
            System.out.println("TCPClientConnection - close client socket " + e);
            e.printStackTrace();
        }
    }
}