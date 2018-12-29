package app.client.host.multiHost;

import app.Utils.ActionUtils;
import app.Utils.FileList;
import app.Utils.Logger;
import app.client.host.ClientCommand;
import app.config.Config;

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
                pull(clientNumber, connectionSocket, clientSentence);
                break;
            default:
                Logger.clientLog('"' + command + '"' + " command is not supported yet");
                break;
        }

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
            System.out.println("TCPClientActionMH - creating outputStream " + e);
            e.printStackTrace();
        }

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
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                // TODO remove sout in catch brackets or add another one here
                e.printStackTrace();
            }

            int count;
            byte[] buffer = new byte[Config.BUFFER_SIZE_IN_BYTES];
            try {
                while ((count = fileInputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, count);
                }
            } catch (IOException e) {
                // TODO remove sout in catch brackets or add another one here
                e.printStackTrace();
            }

            try {
                outputStream.close();
            } catch (IOException e) {
                // TODO remove sout in catch brackets or add another one here
                e.printStackTrace();
            }

            try {
                fileInputStream.close();
            } catch (IOException e) {
                // TODO remove sout in catch brackets or add another one here
                e.printStackTrace();
            }

            Logger.clientLog("Send file " + fileName + " to client " + targetClientNumber);

        } else {

            response = "Client " + targetClientNumber + " doesn't share file " + fileName +
                    ". Chceck file name and client number";
            try {
                outToServer.writeBytes(response + "\n");
            } catch (IOException e) {
                System.out.println("TCPClientActionMH - write to client " + e);
                e.printStackTrace();
            }

            // TODO something that close waiting for file
            try {
//                        byte[] buffer = "fake".getBytes(); // TODO correct that !?
//                        file = null; //TODO TEST -> IS IT NEEDED?
                byte[] buffer = new byte[Config.BUFFER_SIZE_IN_BYTES]; // TODO correct that !?
                outputStream.write(buffer);
            } catch (IOException e) {
                System.out.println("Error: " + e);
            }
            Logger.clientDebugLog(command + " send fake file");

            try {
                outputStream.close();
            } catch (IOException e) {
                // TODO remove sout in catch brackets or add another one here
                e.printStackTrace();
            }
            Logger.clientDebugLog(command + " close outputStream"); // TODO close connection is needed to close file sending...
        }

        Logger.clientDebugLog(command + " sending sequence ended");
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
