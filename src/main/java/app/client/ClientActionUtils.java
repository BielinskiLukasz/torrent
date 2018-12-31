package app.client;

import app.client.host.ClientCommand;
import app.config.Config;
import app.server.ServerCommand;
import app.utils.ConnectionUtils;
import app.utils.ConsoleCommandUtils;
import app.utils.FileList;
import app.utils.Logger;
import app.utils.MD5Sum;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

public class ClientActionUtils {

    public static void uploadIfFileExist(int sourceClientNumber, int targetClientNumber, String fileName) {
        if (isClientHaveFile(sourceClientNumber, fileName)) {
            upload(sourceClientNumber, targetClientNumber, fileName);
        } else {
            Logger.consoleLog("Haven't selected file");
        }
    }

    public static void uploadIfFileExist(int sourceClientNumber, Socket targetClientSocket, String fileName,
                                         long receivedFilePartSize) {
        if (isClientHaveFile(sourceClientNumber, fileName)) {
            upload(sourceClientNumber, targetClientSocket, fileName, receivedFilePartSize);
        } else {
            Logger.consoleLog("You haven't selected file");
        }
    }

    private static void upload(int sourceClientNumber, int targetClientNumber, String fileName) {
        String filePath = Config.BASIC_PATH + sourceClientNumber + "//" + fileName;
        File file = new File(filePath);

        Socket hostConnectionSocket = ConnectionUtils.createSocket(Config.HOST_IP,
                Config.PORT_NR + targetClientNumber);

        DataOutputStream outToClient = ConnectionUtils.getDataOutputStream(hostConnectionSocket);
        String command = String.valueOf(ClientCommand.HANDLE_PUSH);
        ConnectionUtils.sendMessageToDataOutputStream(outToClient, command, String.valueOf(sourceClientNumber), fileName);

        String md5sum = MD5Sum.md5(filePath);
        String response = "Sending file " + fileName + " md5 sum";
        ConnectionUtils.sendMessageToDataOutputStream(outToClient, command, md5sum, response);

        FileInputStream fileInputStream = ConnectionUtils.createFileInputStream(file);
        OutputStream outputStream = ConnectionUtils.getOutputStream(hostConnectionSocket);
        ConnectionUtils.sendFileByStream(fileInputStream, outputStream);
        ConnectionUtils.closeFileInputStream(fileInputStream);

        Logger.consoleLog("Send file " + fileName + " to client " + targetClientNumber);

        ConnectionUtils.closeSocket(hostConnectionSocket);

        Logger.consoleLog("Finished");
    }

    private static void upload(int sourceClientNumber, Socket targetClientSocket, String fileName,
                               long receivedFilePartSize) {
        String filePath = Config.BASIC_PATH + sourceClientNumber + "//" + fileName;
        File file = new File(filePath);

        FileInputStream fileInputStream = ConnectionUtils.createFileInputStream(file);

        try {
            Logger.clientDebugLog("Skip " + receivedFilePartSize + " bytes");
            fileInputStream.skip(receivedFilePartSize);
        } catch (IOException e) {
            e.printStackTrace();
        }

        OutputStream outputStream = ConnectionUtils.getOutputStream(targetClientSocket);
        ConnectionUtils.sendFileByStream(fileInputStream, outputStream);
        ConnectionUtils.closeFileInputStream(fileInputStream);

        Logger.consoleLog("Resend file " + fileName + " to client ");

        ConnectionUtils.closeSocket(targetClientSocket);

        Logger.consoleLog("Finished");
    }

    private static boolean isClientHaveFile(int clientNumber, String fileName) {
        List<String> clientFileNameList = FileList.getFileNameList(clientNumber);
        return clientFileNameList.contains(fileName);
    }

    public static boolean isSelectedClientConnected(int sourceClientNumber) {
        Socket connectionSocket = ConnectionUtils.createSocket(Config.HOST_IP, Config.PORT_NR);

        DataOutputStream outToServer = ConnectionUtils.getDataOutputStream(connectionSocket);
        ConnectionUtils.sendMessageToDataOutputStream(outToServer,
                String.valueOf(ServerCommand.CONFIRM_CONNECTION),
                String.valueOf(sourceClientNumber));

        BufferedReader inFromServer = ConnectionUtils.getBufferedReader(connectionSocket);
        String response = ConnectionUtils.readBufferedReaderLine(inFromServer);
        boolean sourceClientConnected = ConsoleCommandUtils.getBoolean(response);

        ConnectionUtils.closeSocket(connectionSocket);
        return sourceClientConnected;
    }
}
