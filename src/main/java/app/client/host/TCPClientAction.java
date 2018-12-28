package app.client.host;

import app.Utils.ActionUtils;
import app.Utils.Config;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

class TCPClientAction {

    static void perform(int clientNumber, Socket connectionSocket, String clientSentence) {

        String command = ActionUtils.getCommand(clientSentence);

        switch (CommandClient.valueOf(command)) {
            case CONNECT:
                connect(clientNumber, connectionSocket, clientSentence);
                break;

            case FILES_LIST:
                DataOutputStream outToServer = null;
                try {
                    outToServer = new DataOutputStream(connectionSocket.getOutputStream());
                } catch (IOException e) {
                    System.out.println("TCPClientAction - creating dataOutputStream " + e);
                    e.printStackTrace();
                }
                System.out.println(command + " input: " + clientSentence); // TODO debug log

//                String command = ActionUtils.getCommand(clientSentence);
                String response = "response"; // TODO test
                try {
                    outToServer.writeBytes(command + Config.SENTENCE_SPLITS_CHAR + response + "\n");
                } catch (IOException e) {
                    System.out.println("TCPClientAction - write to server " + e);
                    e.printStackTrace();
                }
                System.out.println(command + " input: " + response); // TODO debug log

                break;

            default:
                System.out.println('"' + command + '"' + " command is not supported yet"); // TODO debug log
                break;
        }
    }

    private static void connect(int clientNumber, Socket connectionSocket, String clientSentence) {
        DataOutputStream outToServer = null;
        try {
            outToServer = new DataOutputStream(connectionSocket.getOutputStream());
        } catch (IOException e) {
            System.out.println("TCPClientAction - creating dataOutputStream " + e);
            e.printStackTrace();
        }

        String command = ActionUtils.getCommand(clientSentence);
        String message = ActionUtils.getMessage(clientSentence);
        System.out.println(command + " output: " + message); // TODO debug log
        try {
            outToServer.writeBytes(command + Config.SENTENCE_SPLITS_CHAR + message +
                    Config.SENTENCE_SPLITS_CHAR + clientNumber + "\n");
        } catch (IOException e) {
            System.out.println("TCPClientAction - write to server " + e);
            e.printStackTrace();
        }

        BufferedReader inFromServer = null;
        try {
            inFromServer = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        } catch (IOException e) {
            System.out.println("TCPClientAction - creating inputBufferedReader " + e);
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
    }
}
