package app;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPClientConnection extends Thread {

    private TCPClient client;

    TCPClientConnection(TCPClient tcpClient) {
        this.client = tcpClient;
    }

    public void run() {
        ServerSocket hostServerSocket = null;

        try {
            hostServerSocket = new ServerSocket(Config.PORT_NR + client.clientNumber);
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

                    TCPClientAction.perform(client.clientNumber, connectionSocket, clientSentence);
                }
            } catch (IOException e) {
                System.out.println("TCPClientConnection - read line " + e);
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
        DataOutputStream outToServer = null;
        BufferedReader inFromServer = null;

        try {
            hostClientSocket = new Socket(Config.HOST_IP, Config.PORT_NR);
            outToServer = new DataOutputStream(hostClientSocket.getOutputStream());
            inFromServer = new BufferedReader(new InputStreamReader(hostClientSocket.getInputStream()));

        } catch (IOException e) {
            System.out.println("TCPClientConnection - connect with server " + e);
            e.printStackTrace();
        }

        // TODO implement adding user, files on server side

        try {
            hostClientSocket.close();
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }
    }
}
