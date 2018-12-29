package app.server;

import app.Utils.ActionUtils;
import app.Utils.Config;
import app.Utils.Logger;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

class TCPServerAction {

    static void perform(TCPServer server, Socket connectionSocket, String clientSentence) {
        String command = ActionUtils.getCommand(clientSentence);

        switch (CommandServer.valueOf(command)) {
            case CONNECT:
                connect(server, connectionSocket, clientSentence);
                break;

            case FILE_LIST:
                getFileList(server, connectionSocket, command);
                break;

            default:
                sendNotSupportedCommandMessage(connectionSocket, command);
                break;
        }
    }

    private static void getFileList(TCPServer server, Socket connectionSocket, String command) {
        Logger.serverDebugLog("fire getFileList");

        Logger.serverDebugLog(command + " input: " + "no message");

        List<String> serverFileList = new ArrayList<>();

        server.getUserList().forEach(
                userNumber -> {
                    Socket userSocket = null;
                    try {
                        userSocket = new Socket(Config.HOST_IP, Config.PORT_NR + userNumber);
                    } catch (IOException e) {
                        System.out.println(
                                "TCPServerAction - creating socket " + e);
                        e.printStackTrace();
                    }

                    DataOutputStream outToClient = null;
                    try {
                        outToClient = new DataOutputStream(userSocket.getOutputStream());
                    } catch (IOException e) {
                        System.out.println(
                                "TCPServerAction - creating dataOutputStream " + e);
                        e.printStackTrace();
                    }

                    Logger.serverDebugLog(command + " output: " + "no message");
                    try {
                        outToClient.writeBytes(command + "\n");
                    } catch (IOException e) {
                        System.out.println("TCPServerAction - write to client " + e);
                        e.printStackTrace();
                    }

                    BufferedReader inFromClient = null;
                    try {
                        inFromClient = new BufferedReader(new InputStreamReader(userSocket.getInputStream()));
                    } catch (IOException e) {
                        System.out.println(
                                "TCPServerAction - creating inputBufferedReader " + e);
                        e.printStackTrace();
                    }

                    String response = null;
                    try {
                        response = inFromClient.readLine();
                    } catch (IOException e) {
                        System.out.println("TCPServerAction - read from client (clientFileList size) " + e);
                        e.printStackTrace();
                    }
                    Logger.serverDebugLog(command + " input: " + response);

                    // TODO implement action adding files to fileList
                    int clientFileListSize = ActionUtils.getListSize(response);
                    for (int i = 0; i < clientFileListSize; i++) {
                        try {
                            serverFileList.add(
                                    inFromClient.readLine()
                            );
                        } catch (IOException e) {
                            System.out.println("TCPServerAction - read from client (specific clientFile) " + e);
                            e.printStackTrace();
                        }
                    }

                    try {
                        userSocket.close();
                    } catch (IOException e) {
                        System.out.println("Error: " + e);
                    }
                }
        );

        server.setFileList(serverFileList);

        DataOutputStream outToClient = null;
        try {
            outToClient = new DataOutputStream(connectionSocket.getOutputStream());
        } catch (IOException e) {
            System.out.println("TCPServerAction - creating outputStream " + e);
            e.printStackTrace();
        }

        String response = String.valueOf(serverFileList.size());
        Logger.serverDebugLog(command + " output: " + response);
        try {
            outToClient.writeBytes(command + Config.SENTENCE_SPLITS_CHAR + response + "\n");
        } catch (IOException e) {
            System.out.println("TCPClientAction - write to server " + e);
            e.printStackTrace();
        }

        DataOutputStream finalOutToServer = outToClient;
        serverFileList.forEach(
                fileData -> {
                    try {
                        finalOutToServer.writeBytes(fileData + "\n");
                    } catch (IOException e) {
                        System.out.println("TCPClientAction - write to server (specific clientFile)" + e);
                        e.printStackTrace();
                    }
                    Logger.serverDebugLog(command + " input: " + fileData);
                }
        );

        Logger.serverLog("Server file list sent to client ");
    }

    private static void connect(TCPServer server, Socket connectionSocket, String clientSentence) {
        Logger.serverDebugLog("fire connect");

        DataOutputStream outToClient = null;
        try {
            outToClient = new DataOutputStream(connectionSocket.getOutputStream());
        } catch (IOException e) {
            System.out.println("TCPServerAction - creating outputStream " + e);
            e.printStackTrace();
        }

        String command = ActionUtils.getCommand(clientSentence);
        String message = ActionUtils.getMessage(clientSentence);
        int clientNumber = ActionUtils.getClientNumber(clientSentence);
        Logger.serverDebugLog(command + " input: " + message);

        server.addClient(clientNumber);

        String response = "Hello client " + clientNumber;
        Logger.serverDebugLog(command + " output: " + response);

        try {
            outToClient.writeBytes(response + "\n");
        } catch (IOException e) {
            System.out.println("TCPClientAction - write to server " + e);
            e.printStackTrace();
        }

        Logger.serverLog("Connection to client " + clientNumber + " was detected");
    }

    private static void sendNotSupportedCommandMessage(Socket connectionSocket, String command) {
        Logger.serverDebugLog("fire sendNotSupportedCommandMessage");

        DataOutputStream outToClient = null;
        try {
            outToClient = new DataOutputStream(connectionSocket.getOutputStream());
        } catch (IOException e) {
            System.out.println("TCPServerAction - creating outputStream " + e);
            e.printStackTrace();
        }

        String response = '"' + command + '"' + " command is not supported yet";
        Logger.serverDebugLog(command + " output: " + response);

        try {
            outToClient.writeBytes(response + "\n");
        } catch (IOException e) {
            System.out.println("TCPClientAction - write to server " + e);
            e.printStackTrace();
        }

        Logger.serverLog("Not supported command message sent");
    }
}
