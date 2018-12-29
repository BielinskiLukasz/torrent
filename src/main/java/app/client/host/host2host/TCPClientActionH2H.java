package app.client.host.host2host;

import app.Utils.ActionUtils;
import app.Utils.FileList;
import app.Utils.Logger;
import app.client.host.ClientCommand;
import app.config.Config;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.List;

public class TCPClientActionH2H {

    public static void perform(TCPClientH2H client, Socket connectionSocket, String clientSentence) {

        String command = ActionUtils.getCommand(clientSentence);

        switch (ClientCommand.valueOf(command)) {
            case CONNECT:
                connect(client, connectionSocket, clientSentence);
                break;
            case FILE_LIST:
                getFileList(client.getClientNumber(), connectionSocket, clientSentence);
                break;
            default:
                Logger.clientLog('"' + command + '"' + " command is not supported yet");
                break;
        }
    }

    private static void connect(TCPClientH2H client, Socket connectionSocket, String clientSentence) {
        Logger.clientDebugLog("fire connect");

        DataOutputStream outToServer = null;
        try {
            outToServer = new DataOutputStream(connectionSocket.getOutputStream());
        } catch (IOException e) {
            System.out.println("TCPClientActionMH - creating dataOutputStream " + e);
            e.printStackTrace();
        }

        int connectedClientNumber = ActionUtils.getClientNumber(clientSentence);
        client.setConnectedClientNumber(connectedClientNumber);

        String command = ActionUtils.getCommand(clientSentence);
        String message = ActionUtils.getMessage(clientSentence);
        Logger.clientDebugLog(command + " output: " + message);
        try {
            outToServer.writeBytes(command + Config.SENTENCE_SPLITS_CHAR + client.getClientNumber() +
                    Config.SENTENCE_SPLITS_CHAR + message + "\n");
        } catch (IOException e) {
            System.out.println("TCPClientActionMH - write to host " + e);
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
            System.out.println("TCPClientActionMH - read from host " + e);
            e.printStackTrace();
        }
        Logger.clientDebugLog(command + " input: " + response);

        Logger.clientLog("Client " + client.getClientNumber() + " has connected to the host");
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
