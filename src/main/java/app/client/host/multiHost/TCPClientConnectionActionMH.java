package app.client.host.multiHost;

import app.client.ClientActionUtils;
import app.client.host.ClientCommand;
import app.config.Config;
import app.server.ServerCommand;
import app.utils.ConnectionUtils;
import app.utils.ConsoleCommandUtils;
import app.utils.ExceptionHandler;
import app.utils.FileList;
import app.utils.Logger;
import app.utils.MD5Sum;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.List;

import static java.lang.Thread.sleep;

public class TCPClientConnectionActionMH {

    public static void perform(int clientNumber, Socket connectionSocket, String sentence) {
        Logger.clientDebugLog("perform: " + sentence);

        String command = ConsoleCommandUtils.getCommand(sentence);

        switch (ClientCommand.valueOf(command)) {
            case CONNECT:
                connect(clientNumber, connectionSocket, sentence);
                break;
            case CLIENT_FILE_LIST:
                getFileList(clientNumber, connectionSocket, sentence);
                break;
            case HANDLE_PUSH:
                handlePush(clientNumber, connectionSocket, sentence);
                break;
            case PUSH_ON_DEMAND:
                pushOnDemand(clientNumber, sentence);
                break;
            case REPUSH:
                repush(clientNumber, connectionSocket, sentence);
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
        String command = String.valueOf(ServerCommand.REGISTER);
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

    private static void handlePush(int clientNumber, Socket connectionSocket, String clientSentence) {
        Logger.clientDebugLog("fire handlePush");

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
            handleRepush(clientNumber, connectionSocket, fileMD5Sum, clientSentence, 0);
            /*if (file.delete()) {
                Logger.clientDebugLog("Removed invalid file");
            }*/ // TODO old code (deleting file) remove
        }

        Logger.clientDebugLog(command + " downloading sequence ended");
    }

    private static void handleRepush(int clientNumber,
                                     Socket connectionSocket,
                                     String fileMD5Sum,
                                     String clientSentence,
                                     int reconnectCounter) {
        Logger.clientDebugLog("fire handleRepush");

        int sourceClientNumber = ConsoleCommandUtils.getClientNumber(clientSentence);
        String fileName = ConsoleCommandUtils.getFileName(clientSentence);
        String filePath = Config.BASIC_PATH + clientNumber + "//" + fileName;
        File file = new File(filePath);

        ConnectionUtils.closeSocket(connectionSocket);
        Long receivedFilePartSize = file.length();
        Logger.clientDebugLog("Downloaded " + receivedFilePartSize + " bytes");

        boolean reconnect = false;
        for (int i = 0; i < Config.WAITING_TIME_SEC; i++) {
            try {
                Logger.clientDebugLog("Try reconnect with client " + sourceClientNumber);
                connectionSocket = new Socket(Config.HOST_IP, Config.PORT_NR + sourceClientNumber);
                reconnect = true;
                break;
            } catch (IOException e) {
                ExceptionHandler.handle(e);
            }

            try { // TODO sleep
                sleep(1000);
            } catch (InterruptedException e) {
                ExceptionHandler.handle(e);
            }
        }
        if (reconnect) {
            Logger.clientLog("Reconnected");

            DataOutputStream outToClient = ConnectionUtils.getDataOutputStream(connectionSocket);
            String command = String.valueOf(ClientCommand.REPUSH);
            ConnectionUtils.sendMessageToDataOutputStream(outToClient,
                    command,
                    String.valueOf(clientNumber),
                    fileName, String.valueOf(receivedFilePartSize));
            Logger.clientDebugLog("Repush request sended");

            FileOutputStream fileOutputStream = ConnectionUtils.createFileOutputStream(file, true);
            InputStream inputStream = ConnectionUtils.getInputStream(connectionSocket);
            ConnectionUtils.readFileFromStream(fileOutputStream, inputStream);
            ConnectionUtils.closeFileOutputStream(fileOutputStream);

            Logger.clientDebugLog("End downloading file " + fileName);

            if (MD5Sum.check(filePath, fileMD5Sum)) {
                Logger.clientLog("File downloaded successfully");
            } else {
                Logger.clientLog("Unsuccessful file download");
                if (reconnectCounter++ > Config.MAX_NUMBER_OF_RECONNECT)
                    handleRepush(clientNumber, connectionSocket, fileMD5Sum, clientSentence, reconnectCounter);
            }
        } else {
            Logger.clientLog("Cannot reconnect with client " + sourceClientNumber);
        }
    }

    private static void pushOnDemand(int clientNumber, String clientSentence) {
        Logger.clientDebugLog("fire pushOnDemand");

        int targetClientNumber = ConsoleCommandUtils.getClientNumber(clientSentence);
        String fileName = ConsoleCommandUtils.getFileName(clientSentence);

        ClientActionUtils.uploadIfFileExist(clientNumber, targetClientNumber, fileName);
    }

    private static void repush(int clientNumber, Socket connectionSocket, String clientSentence) {
        Logger.clientDebugLog("fire repush");

        String fileName = ConsoleCommandUtils.getFileName(clientSentence);
        long receivedFilePartSize = ConsoleCommandUtils.getStartByteNumber(clientSentence);

        ClientActionUtils.uploadIfFileExist(clientNumber, connectionSocket, fileName, receivedFilePartSize);
    }


}
