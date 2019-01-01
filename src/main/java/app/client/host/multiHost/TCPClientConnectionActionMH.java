package app.client.host.multiHost;

import app.client.ActionUtils;
import app.client.host.ClientCommand;
import app.config.Config;
import app.server.ServerCommand;
import app.utils.ExceptionHandler;
import app.utils.FileList;
import app.utils.Logger;
import app.utils.MD5Sum;
import app.utils.SentenceUtils;
import app.utils.TCPConnectionUtils;

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

        String command = SentenceUtils.getCommand(sentence);

        switch (ClientCommand.valueOf(command)) {
            case CONNECT:
                connect(clientNumber, connectionSocket, sentence);
                break;
            case CLIENT_FILE_LIST:
                getClientFileList(clientNumber, connectionSocket, sentence);
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
            case CHECK_SENDING:
                checkingSendingCorrectness(clientNumber, sentence);
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
        DataOutputStream outToServer = TCPConnectionUtils.getDataOutputStream(connectionSocket);
        String command = String.valueOf(ServerCommand.REGISTER);
        String message = SentenceUtils.getMessage(clientSentence);
        TCPConnectionUtils.sendMessageToDataOutputStream(outToServer, command, String.valueOf(clientNumber), message);
    }

    private static void receiveConfirmationOfConnection(Socket connectionSocket) {
        BufferedReader inFromServer = TCPConnectionUtils.getBufferedReader(connectionSocket);
        TCPConnectionUtils.readBufferedReaderLine(inFromServer);
    }

    private static void getClientFileList(int clientNumber, Socket connectionSocket, String clientSentence) {
        Logger.clientDebugLog("fire getClientFileList");

        String command = SentenceUtils.getCommand(clientSentence);
        Logger.clientDebugLog(command + " input: " + clientSentence);

        List<String> clientFileList = FileList.packFileInfoList(
                FileList.getFileInfoList(clientNumber)
        );

        ActionUtils.sendList(connectionSocket, clientFileList);

        Logger.clientLog("Client file list sent to server");
    }

    private static void handlePush(int clientNumber, Socket connectionSocket, String clientSentence) {
        Logger.clientDebugLog("fire handlePush");

        String command = SentenceUtils.getCommand(clientSentence);
        int sourceClientNumber = SentenceUtils.getClientNumber(clientSentence);
        String fileName = SentenceUtils.getFileName(clientSentence);
        Logger.clientLog("Receiving file " + fileName + " from client " + sourceClientNumber);

        BufferedReader inFromClient = TCPConnectionUtils.getBufferedReader(connectionSocket);
        String sentence = TCPConnectionUtils.readBufferedReaderLine(inFromClient);
        String fileMD5Sum = SentenceUtils.getMD5Sum(sentence);

        String filePath = Config.BASIC_PATH + clientNumber + "//" + fileName;
        File file = new File(filePath);
        FileOutputStream fileOutputStream = TCPConnectionUtils.createFileOutputStream(file);
        InputStream inputStream = TCPConnectionUtils.getInputStream(connectionSocket);
        TCPConnectionUtils.readFileFromStream(fileOutputStream, inputStream);
        TCPConnectionUtils.closeFileOutputStream(fileOutputStream);

        if (MD5Sum.check(filePath, fileMD5Sum)) {
            Logger.clientLog("File downloaded successfully");
        } else {
            Logger.clientLog("Unsuccessful file download");
            invokeRepush(clientNumber, connectionSocket, fileMD5Sum, clientSentence, 0);
        }

        Logger.clientDebugLog(command + " downloading sequence ended");
    }

    private static void checkingSendingCorrectness(int clientNumber, String sentence) {
        Logger.clientDebugLog("fire checkingSendingCorrectness");

        String fileName = SentenceUtils.getFileName(sentence);
        String fileMD5Sum = SentenceUtils.getMD5Sum(sentence);
        String filePath = Config.BASIC_PATH + clientNumber + "//" + fileName;
        if (MD5Sum.check(filePath, fileMD5Sum)) {
            Logger.clientLog("Confirmation of the correctness of sending");
        } else {
            int sourceClientNumber = SentenceUtils.getClientNumber(sentence);
            Socket connectionSocket = TCPConnectionUtils.createSocket(Config.HOST_IP,
                    Config.PORT_NR + sourceClientNumber);

            DataOutputStream outToClient = TCPConnectionUtils.getDataOutputStream(connectionSocket);
            String command = String.valueOf(ClientCommand.REPUSH);
            TCPConnectionUtils.sendMessageToDataOutputStream(outToClient,
                    command,
                    String.valueOf(clientNumber),
                    fileName,
                    String.valueOf((new File(filePath)).length()));
            Logger.clientDebugLog("Repush request sended");

            handleRepush(clientNumber, connectionSocket, fileMD5Sum, sentence, 0);
        }
    }

    private static void invokeRepush(int clientNumber,
                                     Socket connectionSocket,
                                     String fileMD5Sum,
                                     String clientSentence,
                                     int reconnectCounter) {
        Logger.clientDebugLog("fire invokeRepush");

        int sourceClientNumber = SentenceUtils.getClientNumber(clientSentence);
        String fileName = SentenceUtils.getFileName(clientSentence);
        String filePath = Config.BASIC_PATH + clientNumber + "//" + fileName;
        File file = new File(filePath);

        TCPConnectionUtils.closeSocket(connectionSocket);
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

            DataOutputStream outToClient = TCPConnectionUtils.getDataOutputStream(connectionSocket);
            String command = String.valueOf(ClientCommand.REPUSH);
            TCPConnectionUtils.sendMessageToDataOutputStream(outToClient,
                    command,
                    String.valueOf(clientNumber),
                    fileName,
                    String.valueOf(receivedFilePartSize));
            Logger.clientDebugLog("Repush request sended");

            handleRepush(clientNumber, connectionSocket, fileMD5Sum, clientSentence, reconnectCounter);
        } else {
            Logger.clientLog("Cannot reconnect with client " + sourceClientNumber);
        }
    }

    private static void handleRepush(int clientNumber,
                                     Socket connectionSocket,
                                     String fileMD5Sum,
                                     String clientSentence,
                                     int reconnectCounter) {
        Logger.clientDebugLog("fire handleRepush");

        String fileName = SentenceUtils.getFileName(clientSentence);
        String filePath = Config.BASIC_PATH + clientNumber + "//" + fileName;
        File file = new File(filePath);

        FileOutputStream fileOutputStream = TCPConnectionUtils.createFileOutputStream(file, true);
        InputStream inputStream = TCPConnectionUtils.getInputStream(connectionSocket);
        TCPConnectionUtils.readFileFromStream(fileOutputStream, inputStream);
        TCPConnectionUtils.closeFileOutputStream(fileOutputStream);
        Logger.clientDebugLog("End downloading file " + fileName);

        if (MD5Sum.check(filePath, fileMD5Sum)) {
            Logger.clientLog("File downloaded successfully");
        } else {
            Logger.clientLog("Unsuccessful file download");
            if (reconnectCounter++ > Config.MAX_NUMBER_OF_RECONNECT)
                invokeRepush(clientNumber, connectionSocket, fileMD5Sum, clientSentence, reconnectCounter);
        }
    }

    private static void pushOnDemand(int clientNumber, String clientSentence) {
        Logger.clientDebugLog("fire pushOnDemand");

        int targetClientNumber = SentenceUtils.getClientNumber(clientSentence);
        String fileName = SentenceUtils.getFileName(clientSentence);

        ActionUtils.uploadIfFileExist(clientNumber, targetClientNumber, fileName);
    }

    private static void repush(int clientNumber, Socket connectionSocket, String clientSentence) {
        Logger.clientDebugLog("fire repush");

        String fileName = SentenceUtils.getFileName(clientSentence);
        long receivedFilePartSize = SentenceUtils.getStartByteNumber(clientSentence);

        ActionUtils.uploadIfFileExist(clientNumber, connectionSocket, fileName, receivedFilePartSize);
    }
}
