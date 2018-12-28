package app;

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

        System.out.println("TCPServerConnection: create connection"); // TODO debug log
    }

    public void run() {

        System.out.println("TCPServerConnection: run"); // TODO debug log

        BufferedReader inFromClient = null;
        try {
            inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
//            System.out.println("TCPServerConnection: new BufferedReader"); // TODO debug log
        } catch (IOException e) {
            System.out.println("TCPServerConnection - client connection " + e);
            e.printStackTrace();
        }

        while (true) {
            String clientSentence = null;
//            System.out.println("TCPServerConnection: while start"); // TODO debug log
//            System.out.println(connectionSocket != null ? connectionSocket.toString() : null); // TODO debug log

            try {
                clientSentence = inFromClient.readLine();
//                System.out.println("TCPServerConnection: readLine"); // TODO debug log
//                System.out.println(clientSentence); // TODO debug log
            } catch (IOException e) {
                System.out.println("TCPServerConnection - read line " + e);
                e.printStackTrace();
            }

            if (clientSentence != null) {
//                System.out.println(clientSentence); // TODO debug log
                TCPServerAction.perform(server, connectionSocket, clientSentence);
            }

            try { // TODO sleep
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

//            System.out.println("TCPServerConnection: while..."); // TODO debug log
        }
    }
}
