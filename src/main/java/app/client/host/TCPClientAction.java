package app.client.host;

import app.Utils.ActionUtils;
import app.Utils.Config;
import app.server.CommandServer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

class TCPClientAction {

    static void perform(int clientNumber, Socket connectionSocket, String clientSentence) {
        DataOutputStream outToServer = null;
        BufferedReader inFromServer = null;
        try {
            outToServer = new DataOutputStream(connectionSocket.getOutputStream());
            inFromServer = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        } catch (IOException e) {
            System.out.println("TCPClientAction - creating outputStream and inputBufferedReader " + e);
            e.printStackTrace();
        }

        String command = ActionUtils.getCommand(clientSentence);

        switch (CommandClient.valueOf(command)) {
            case CONNECT:
                String message = ActionUtils.getMessage(clientSentence);
                System.out.println(command + " output: " + message); // TODO debug log
                try {
                    outToServer.writeBytes(CommandServer.CONNECT + Config.SENTENCE_SPLITS_CHAR + message +
                            Config.SENTENCE_SPLITS_CHAR + clientNumber + "\n");
                } catch (IOException e) {
                    System.out.println("TCPClientAction - write to server " + e);
                    e.printStackTrace();
                }

                String response = null;
                try {
                    response = inFromServer.readLine();
                } catch (IOException e) {
                    System.out.println("TCPClientAction - read from server " + e);
                    e.printStackTrace();
                }
                System.out.println(command + " input: " + response); // TODO debug log
                break;
            default:
                break;
        }
    }
}
