package app.client.host.multiHost;

import app.client.host.ClientCommand;
import app.config.Config;
import app.utils.ActionUtils;
import app.utils.ConnectionUtils;
import app.utils.FileList;
import app.utils.Logger;
import app.utils.MD5Sum;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
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

    private static void connect(int clientNumber, Socket connectionSocket, String clientSentence) {
        Logger.clientDebugLog("fire connect");

        sendConnectionRequest(connectionSocket, clientNumber, clientSentence);
        receiveConfirmationOfConnection(connectionSocket);

        Logger.clientLog("Client " + clientNumber + " has connected to the server");
    }

    private static void sendConnectionRequest(Socket connectionSocket, int clientNumber, String clientSentence) {
        DataOutputStream outToServer = ConnectionUtils.getDataOutputStream(connectionSocket);
        String command = ActionUtils.getCommand(clientSentence);
        String message = ActionUtils.getMessage(clientSentence);
        ConnectionUtils.sendMessageToDataOutputStream(outToServer, command, String.valueOf(clientNumber), message);
    }

    private static void receiveConfirmationOfConnection(Socket connectionSocket) {
        BufferedReader inFromServer = ConnectionUtils.getBufferedReader(connectionSocket);
        ConnectionUtils.readBufferedReaderLine(inFromServer);
    }

    private static void getFileList(int clientNumber, Socket connectionSocket, String clientSentence) {
        Logger.clientDebugLog("fire getFileList");

        String command = ActionUtils.getCommand(clientSentence);
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

    private static void pull(int clientNumber, Socket connectionSocket, String clientSentence) {
        Logger.clientDebugLog("fire pull");

        String command = ActionUtils.getCommand(clientSentence);
        int targetClientNumber = ActionUtils.getClientNumber(clientSentence);
        String fileName = ActionUtils.getFileName(clientSentence);


        String filePath = Config.BASIC_PATH + clientNumber + "//" + fileName;
        File file = new File(filePath);

        String response;
        if (file.exists()) {
            response = "Sending file " + fileName + " started";
        } else {
            response = "Client " + clientNumber + " doesn't share file " + fileName +
                    ". Chceck file name and client number";
        }

        DataOutputStream outToServer = ConnectionUtils.getDataOutputStream(connectionSocket);
        ConnectionUtils.sendMessageToDataOutputStream(outToServer, command, String.valueOf(file.exists()), response);

        if (file.exists()) {
            String md5sum = MD5Sum.md5(filePath);
            response = "Sending file " + fileName + " md5 sum";
            ConnectionUtils.sendMessageToDataOutputStream(outToServer, command, md5sum, response);

            FileInputStream fileInputStream = ConnectionUtils.createFileInputStream(file);
            OutputStream outputStream = ConnectionUtils.getOutputStream(connectionSocket);
            ConnectionUtils.sendFileByStream(fileInputStream, outputStream);
            ConnectionUtils.closeFileInputStream(fileInputStream);

            Logger.clientLog("Send file " + fileName + " to client " + targetClientNumber);
        }

        ConnectionUtils.closeSocket(connectionSocket);

        Logger.clientDebugLog(command + " sending sequence ended");
    }
}
