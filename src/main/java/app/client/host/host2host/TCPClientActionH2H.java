package app.client.host.host2host;

import app.client.host.ClientCommand;
import app.config.Config;
import app.utils.ActionUtils;
import app.utils.FileList;
import app.utils.Logger;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
            case PULL:
                pull(client.getClientNumber(), connectionSocket, clientSentence); // TODO check it
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
            e.printStackTrace();
        }

        int connectedClientNumber = ActionUtils.getClientNumber(clientSentence);
        client.setConnectedClientNumber(connectedClientNumber);

        String command = ActionUtils.getCommand(clientSentence);
        String message = ActionUtils.getMessage(clientSentence);
        Logger.clientDebugLog(command + " output: " + message);
        try {
            outToServer.writeBytes(command + Config.SPLITS_CHAR + client.getClientNumber() +
                    Config.SPLITS_CHAR + message + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedReader inFromServer = null;
        try {
            inFromServer = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String response = null;
        try {
            response = inFromServer.readLine();
        } catch (IOException e) {
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
            e.printStackTrace();
        }
        Logger.clientDebugLog(command + " input: " + clientSentence);

        List<String> clientFileList = FileList.packFileInfoList(
                FileList.getFileInfoList(clientNumber)
        );

        String response = String.valueOf(clientFileList.size());
        try {
            outToServer.writeBytes(command + Config.SPLITS_CHAR + response + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Logger.clientDebugLog(command + " input: " + response);

        DataOutputStream finalOutToServer = outToServer;
        clientFileList.forEach(
                fileData -> {
                    try {
                        finalOutToServer.writeBytes(fileData + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Logger.clientDebugLog(command + " input: " + fileData);
                }
        );

        Logger.clientLog("Client file list sent to server");
    }

    private static void pull(int clientNumber, Socket connectionSocket, String clientSentence) {
        Logger.clientDebugLog("fire pull");

        String command = ActionUtils.getCommand(clientSentence);
        int targetClientNumber = ActionUtils.getClientNumber(clientSentence);
        String fileName = ActionUtils.getFileName(clientSentence);
        String response;

        OutputStream outputStream = null;
        try {
            outputStream = connectionSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        DataOutputStream outToServer = null;
        try {
            outToServer = new DataOutputStream(connectionSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        File file = new File(Config.BASIC_PATH + clientNumber + "//" + fileName);
        if (file.exists()) {

            response = "Sending file " + fileName + " started";
            try {
                outToServer.writeBytes(response + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }

            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            int count;
            byte[] buffer = new byte[Config.BUFFER_SIZE_IN_BYTES];
            try {
                while ((count = fileInputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, count);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Logger.clientLog("Send file " + fileName + " to client " + targetClientNumber);

        } else {

            response = "Client " + clientNumber + " doesn't share file " + fileName +
                    ". Chceck file name and client number";
            try {
                outToServer.writeBytes(response + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                byte[] buffer = new byte[Config.BUFFER_SIZE_IN_BYTES];
                outputStream.write(buffer);
            } catch (IOException e) {
                System.out.println("Error: " + e);
            }
            Logger.clientDebugLog(command + " send fake file");

            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Logger.clientDebugLog(command + " close outputStream");
        }

        Logger.clientDebugLog(command + " sending sequence ended");
    }
}
