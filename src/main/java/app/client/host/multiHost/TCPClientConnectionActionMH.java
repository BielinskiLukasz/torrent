package app.client.host.multiHost;

import app.client.ClienActionUtils;
import app.client.host.ClientCommand;
import app.config.Config;
import app.utils.ConnectionUtils;
import app.utils.ConsoleCommandUtils;
import app.utils.FileList;
import app.utils.Logger;
import app.utils.MD5Sum;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.Socket;
import java.util.List;

public class TCPClientConnectionActionMH {

    public static void perform(int clientNumber, Socket connectionSocket, String clientSentence) {

        String command = ConsoleCommandUtils.getCommand(clientSentence);

        switch (ClientCommand.valueOf(command)) {
            case CONNECT:
                connect(clientNumber, connectionSocket, clientSentence);
                break;
            case FILE_LIST:
                getFileList(clientNumber, connectionSocket, clientSentence);
                break;
            case PUSH:
                push(clientNumber, connectionSocket, clientSentence);
                break;
            case PUSH_ON_DEMAND:
                pushOnDemand(clientNumber, clientSentence);
                break;
            default:
                Logger.clientLog('"' + command + '"' + " command is not supported yet");
                break;
        }
    }

    private static void connect(int clientNumber, Socket connectionSocket, String clientSentence) {
        Logger.clientDebugLog("fire connect");

        sendConnectionRequest(connectionSocket, clientNumber, clientSentence);
        receiveConfirmationOfConnection(connectionSocket);

        Logger.clientLog("Client " + clientNumber + " has connected to the server");
    }

    private static void sendConnectionRequest(Socket connectionSocket, int clientNumber, String clientSentence) {
        DataOutputStream outToServer = ConnectionUtils.getDataOutputStream(connectionSocket);
        String command = ConsoleCommandUtils.getCommand(clientSentence);
        String message = ConsoleCommandUtils.getMessage(clientSentence);
        ConnectionUtils.sendMessageToDataOutputStream(outToServer, command, String.valueOf(clientNumber), message);
    }

    private static void receiveConfirmationOfConnection(Socket connectionSocket) {
        BufferedReader inFromServer = ConnectionUtils.getBufferedReader(connectionSocket);
        ConnectionUtils.readBufferedReaderLine(inFromServer);
    }

    private static void getFileList(int clientNumber, Socket connectionSocket, String clientSentence) {
        Logger.clientDebugLog("fire getFileList");

        String command = ConsoleCommandUtils.getCommand(clientSentence);
        Logger.clientDebugLog(command + " input: " + clientSentence);

        List<String> clientFileList = FileList.packFileInfoList(
                FileList.getFileInfoList(clientNumber)
        );

        DataOutputStream outToServer = ConnectionUtils.getDataOutputStream(connectionSocket);
        String response = String.valueOf(clientFileList.size());
        ConnectionUtils.sendMessageToDataOutputStream(outToServer, command, response);

        clientFileList.forEach(
                fileData -> ConnectionUtils.sendMessageToDataOutputStream(outToServer, fileData)
        );

        Logger.clientLog("Client file list sent to server");
    }

    private static void push(int clientNumber, Socket connectionSocket, String clientSentence) {
        Logger.clientDebugLog("fire pull");

        String command = ConsoleCommandUtils.getCommand(clientSentence);
        int sourceClientNumber = ConsoleCommandUtils.getClientNumber(clientSentence);
        String fileName = ConsoleCommandUtils.getFileName(clientSentence);
        Logger.clientLog("Receiving file " + fileName + " from client " + sourceClientNumber);

        BufferedReader inFromClient = ConnectionUtils.getBufferedReader(connectionSocket);
        String sentence = ConnectionUtils.readBufferedReaderLine(inFromClient);
        String fileMD5Sum = ConsoleCommandUtils.getMD5Sum(sentence);

        String filePath = Config.BASIC_PATH + clientNumber + "//" + fileName;
        File file = new File(filePath);
        FileOutputStream fileOutputStream = ConnectionUtils.createFileOutputStream(file);
        InputStream inputStream = ConnectionUtils.getInputStream(connectionSocket);
        ConnectionUtils.readFileFromStream(fileOutputStream, inputStream);
        ConnectionUtils.closeFileOutputStream(fileOutputStream);

        if (MD5Sum.check(filePath, fileMD5Sum)) {
            Logger.clientLog("File downloaded successfully");
        } else {
            Logger.clientLog("Unsuccessful file download");
            if (file.delete()) {
                Logger.clientDebugLog("Removed invalid file");
            }
        }

        Logger.clientDebugLog(command + " downloading sequence ended");
    }

    private static void pushOnDemand(int clientNumber, String clientSentence) {
        Logger.clientDebugLog("fire pushOnDemand");

        int targetClientNumber = ConsoleCommandUtils.getClientNumber(clientSentence);
        String fileName = ConsoleCommandUtils.getFileName(clientSentence);

        ClienActionUtils.uploadIfFileExist(clientNumber, targetClientNumber, fileName);
    }
}
