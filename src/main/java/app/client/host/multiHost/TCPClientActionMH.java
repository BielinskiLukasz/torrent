package app.client.host.multiHost;

import app.Utils.ActionUtils;
import app.Utils.FileList;
import app.Utils.Logger;
import app.client.host.ClientCommand;
import app.config.Config;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.List;

public class TCPClientActionMH {

    public static void perform(int clientNumber, Socket connectionSocket, String clientSentence) {

        String command = ActionUtils.getCommand(clientSentence);

        switch (ClientCommand.valueOf(command)) {
            case CONNECT:
                connect(clientNumber, connectionSocket, clientSentence);
                break;
            case FILE_LIST:
                getFileList(clientNumber, connectionSocket, clientSentence);
                break;
            case PULL:

                Logger.clientDebugLog("fire pull");

                int targetClientNumber = ActionUtils.getClientNumber(clientSentence);
                String fileName = ActionUtils.getFileName(clientSentence);
                String response;

                DataOutputStream outToServer = null;
                try {
                    outToServer = new DataOutputStream(connectionSocket.getOutputStream());
                } catch (IOException e) {
                    System.out.println("TCPClientActionMH - creating dataOutputStream " + e);
                    e.printStackTrace();
                }

                File file = new File(Config.BASIC_PATH + clientNumber + "//" + fileName);
                if (file.exists()) {

                    response = "Sending file " + fileName + " started";
                    try {
                        outToServer.writeBytes(response + "\n");
                    } catch (IOException e) {
                        System.out.println("TCPClientActionMH - write to client " + e);
                        e.printStackTrace();
                    }

                    // TODO implements sending file


                    response = "Sending file " + fileName + " finished";
                    try {
                        outToServer.writeBytes(response + "\n");
                    } catch (IOException e) {
                        System.out.println("TCPClientActionMH - write to client " + e);
                        e.printStackTrace();
                    }

                    Logger.clientLog("Send file " + fileName + " to client " + targetClientNumber);

                } else {

                    response = "Client " + targetClientNumber + " doesn't share file " + fileName;
                    try {
                        outToServer.writeBytes(response + "\n");
                    } catch (IOException e) {
                        System.out.println("TCPClientActionMH - write to client " + e);
                        e.printStackTrace();
                    }

                    // TODO something that close waiting for file


                    response = "Check the correctness of the file name and client number";
                    try {
                        outToServer.writeBytes(response + "\n");
                    } catch (IOException e) {
                        System.out.println("TCPClientActionMH - write to client " + e);
                        e.printStackTrace();
                    }
                }

                break;
            default:
                Logger.clientLog('"' + command + '"' + " command is not supported yet");
                break;
        }
    }

    private static void connect(int clientNumber, Socket connectionSocket, String clientSentence) {
        Logger.clientDebugLog("fire connect");

        DataOutputStream outToServer = null;
        try {
            outToServer = new DataOutputStream(connectionSocket.getOutputStream());
        } catch (IOException e) {
            System.out.println("TCPClientActionMH - creating dataOutputStream " + e);
            e.printStackTrace();
        }

        String command = ActionUtils.getCommand(clientSentence);
        String message = ActionUtils.getMessage(clientSentence);
        Logger.clientDebugLog(command + " output: " + message);
        try {
            outToServer.writeBytes(command + Config.SENTENCE_SPLITS_CHAR + clientNumber +
                    Config.SENTENCE_SPLITS_CHAR + message + "\n");
        } catch (IOException e) {
            System.out.println("TCPClientActionMH - write to server " + e);
            e.printStackTrace();
        }

        BufferedReader inFromServer = null;
        try {
            inFromServer = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        } catch (IOException e) {
            System.out.println("TCPClientActionMH - creating inputBufferedReader " + e);
            e.printStackTrace();
        }

        String response = null;
        try {
            response = inFromServer.readLine();
        } catch (IOException e) {
            System.out.println("TCPClientActionMH - read from server " + e);
            e.printStackTrace();
        }
        Logger.clientDebugLog(command + " input: " + response);

        Logger.clientLog("Client " + clientNumber + " has connected to the server");
    }

    private static void getFileList(int clientNumber, Socket connectionSocket, String clientSentence) {
        Logger.clientDebugLog("fire getFileList");

        String command = ActionUtils.getCommand(clientSentence);
        DataOutputStream outToServer = null;
        try {
            outToServer = new DataOutputStream(connectionSocket.getOutputStream());
        } catch (IOException e) {
            System.out.println("TCPClientActionMH - creating dataOutputStream " + e);
            e.printStackTrace();
        }
        Logger.clientDebugLog(command + " input: " + clientSentence);

        List<String> clientFileList = FileList.packFileInfoList(
                FileList.getFileInfoList(clientNumber)
        );

        String response = String.valueOf(clientFileList.size());
        try {
            outToServer.writeBytes(command + Config.SENTENCE_SPLITS_CHAR + response + "\n");
        } catch (IOException e) {
            System.out.println("TCPClientActionMH - write to server (clientFileList size) " + e);
            e.printStackTrace();
        }
        Logger.clientDebugLog(command + " input: " + response);

        DataOutputStream finalOutToServer = outToServer;
        clientFileList.forEach(
                fileData -> {
                    try {
                        finalOutToServer.writeBytes(fileData + "\n");
                    } catch (IOException e) {
                        System.out.println("TCPClientActionMH - write to server (specific clientFile)" + e);
                        e.printStackTrace();
                    }
                    Logger.clientDebugLog(command + " input: " + fileData);
                }
        );

        Logger.clientLog("Client file list sent to server");
    }
}
