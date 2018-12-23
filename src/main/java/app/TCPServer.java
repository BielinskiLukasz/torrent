package app;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {

    private static final String SERVER_TAG = "<SERVER>: ";

    public TCPServer() throws IOException {

        System.out.println(SERVER_TAG + "Server created");

        String clientSentence;
        String responseClientSentence;
        ServerSocket welcomeSocket = new ServerSocket(Config.PORT_NR);

        while (true) {
            Socket connectionSocket = welcomeSocket.accept();
            BufferedReader inFromClient =
                    new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            DataOutputStream outToClient =
                    new DataOutputStream(connectionSocket.getOutputStream());

            clientSentence = inFromClient.readLine() + " - connected";
            System.out.println(SERVER_TAG + clientSentence);
            responseClientSentence = clientSentence + '\n';
            outToClient.writeBytes(responseClientSentence);
        }

    }

}