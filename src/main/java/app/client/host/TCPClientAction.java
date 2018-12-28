package app.client.host;

import app.Utils.ActionUtils;
import app.Utils.Config;
import app.Utils.FileList;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.List;

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

                // TODO implement getting file list
                List<String> clientFileList = FileList.packFileInfoList(
                        FileList.getFileInfoList(clientNumber)
                );

//                String command = ActionUtils.getCommand(clientSentence);
                String response = String.valueOf(clientFileList.size()); // TODO rename string - size of list ???
                try {
                    outToServer.writeBytes(command + Config.SENTENCE_SPLITS_CHAR + response + "\n");
                } catch (IOException e) {
                    System.out.println("TCPClientAction - write to server (clientFileList size) " + e);
                    e.printStackTrace();
                }
                System.out.println(command + " input: " + response); // TODO debug log

                DataOutputStream finalOutToServer = outToServer;
                clientFileList.forEach(
                        fileData -> {
                            try {
                                finalOutToServer.writeBytes(fileData + "\n");
                            } catch (IOException e) {
                                System.out.println("TCPClientAction - write to server (specific clientFile)" + e);
                                e.printStackTrace();
                            }
                            System.out.println(command + " input: " + fileData); // TODO debug log
                        }
                );

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

        System.out.println("Client " + clientNumber + " has connected to the server");
    }
}
