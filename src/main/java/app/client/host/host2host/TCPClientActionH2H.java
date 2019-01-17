package app.client.host.host2host;

import app.client.host.ClientCommand;
import app.config.Config;
import app.utils.ExceptionHandler;
import app.utils.Logger;
import app.utils.connectionUtils.ActionUtils;
import app.utils.connectionUtils.CommandUtils;
import app.utils.connectionUtils.SentenceUtils;
import app.utils.connectionUtils.TCPConnectionUtils;
import app.utils.fileUtils.FileList;
import app.utils.fileUtils.MD5Sum;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.List;
import java.util.Objects;

import static java.lang.Thread.sleep;

class TCPClientActionH2H {

    public static void perform(TCPClientH2H client, Socket connectionSocket, String clientSentence) {

        String command = CommandUtils.getCommand(clientSentence);
        command = CommandUtils.transformToH2HCommand(command);

        switch (ClientCommand.valueOf(command)) {
            case CONNECT:
                connect(client, connectionSocket, clientSentence);
                break;
            case CLIENT_FILE_LIST:
                getFileList(client.getClientNumber(), connectionSocket, clientSentence);
                break;
            case HANDLE_PUSH:
                handlePush(client.getClientNumber(), connectionSocket, clientSentence);
                break;
            case PUSH_ON_DEMAND:
                pushOnDemand(client.getClientNumber(), clientSentence);
                break;
            case RE_PUSH:
                rePush(client.getClientNumber(), clientSentence);
                break;
            case HANDLE_RE_PUSH:
                handleRePush(client.getClientNumber(), connectionSocket, clientSentence);
                break;
            case CHECK_SENDING:
                checkingSendingCorrectness(client.getClientNumber(), clientSentence);
                break;
            default:
                Logger.clientLog('"' + command + '"' + " command is not supported yet");
                break;
        }
    }

    private static void connect(TCPClientH2H client, Socket connectionSocket, String clientSentence) {
        Logger.clientDebugLog("fire connect h2h");

        DataOutputStream outToServer = null;
        try {
            outToServer = new DataOutputStream(connectionSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        int connectedClientNumber = SentenceUtils.getClientNumber(clientSentence);
        if (client.getConnectedClientNumber() == Config.INT_SV) {
            client.setConnectedClientNumber(connectedClientNumber);
        }

        String command = SentenceUtils.getCommand(clientSentence);
        String message = SentenceUtils.getMessage(clientSentence);
        Logger.clientDebugLog(command + " output: " + message);
        try {
            Objects.requireNonNull(outToServer).writeBytes(command + Config.SPLITS_CHAR + client.getClientNumber() +
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
            response = Objects.requireNonNull(inFromServer).readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Logger.clientDebugLog(command + " input: " + response);

        Logger.clientLog("Client " + client.getClientNumber() + " has connected to the host");
    }

    private static void getFileList(int clientNumber, Socket connectionSocket, String clientSentence) {
        Logger.clientDebugLog("fire getFileList");

        String command = String.valueOf(ClientCommand.CLIENT_FILE_LIST);
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
            Objects.requireNonNull(outToServer).writeBytes(command + Config.SPLITS_CHAR + response + "\n");
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
            invokeRePush(clientNumber, connectionSocket, clientSentence);
        }

        Logger.clientDebugLog(command + " downloading sequence ended");
    }

    private static void handleRePush(int clientNumber, Socket connectionSocket, String clientSentence) {
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
        FileOutputStream fileOutputStream = TCPConnectionUtils.createFileOutputStream(file, true);
        InputStream inputStream = TCPConnectionUtils.getInputStream(connectionSocket);
        TCPConnectionUtils.readFileFromStream(fileOutputStream, inputStream);
        TCPConnectionUtils.closeFileOutputStream(fileOutputStream);

        if (MD5Sum.check(filePath, fileMD5Sum)) {
            Logger.clientLog("File downloaded successfully");
        } else {
            Logger.clientLog("Unsuccessful file download");
            invokeRePush(clientNumber, connectionSocket, clientSentence);
        }

        Logger.clientDebugLog(command + " downloading sequence ended");
    }

    private static void checkingSendingCorrectness(int clientNumber, String sentence) {
        Logger.clientDebugLog("fire checkingSendingCorrectness");

        String fileName = SentenceUtils.getFileName(sentence);
        String fileMD5Sum = SentenceUtils.getMD5Sum(sentence);
        String filePath = Config.BASIC_PATH + clientNumber + "//" + fileName;
        if (MD5Sum.check(filePath, fileMD5Sum)) {
            Logger.clientDebugLog("Confirmation of the correctness of sending");
        } else {
            int sourceClientNumber = SentenceUtils.getClientNumber(sentence);
            Socket connectionSocket = TCPConnectionUtils.createSocket(Config.HOST_IP,
                    Config.PORT_NR + sourceClientNumber);

            DataOutputStream outToClient = TCPConnectionUtils.getDataOutputStream(connectionSocket);
            String command = String.valueOf(ClientCommand.RE_PUSH);
            TCPConnectionUtils.writeMessageToDataOutputStream(outToClient,
                    command,
                    String.valueOf(clientNumber),
                    fileName,
                    String.valueOf((new File(filePath)).length()));
            Logger.clientDebugLog("RePush request sended");

            TCPConnectionUtils.closeSocket(connectionSocket);
        }
    }

    private static void invokeRePush(int clientNumber,
                                     Socket connectionSocket,
                                     String clientSentence) {
        Logger.clientDebugLog("fire invokeRePush");

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
            String command = String.valueOf(ClientCommand.RE_PUSH);
            TCPConnectionUtils.writeMessageToDataOutputStream(outToClient,
                    command,
                    String.valueOf(clientNumber),
                    fileName,
                    String.valueOf(receivedFilePartSize));
            Logger.clientDebugLog("Repush request sended");

            TCPConnectionUtils.closeSocket(connectionSocket);
        } else {
            Logger.clientLog("Cannot reconnect with client " + sourceClientNumber);
        }
    }

    private static void pushOnDemand(int clientNumber, String clientSentence) {
        Logger.clientDebugLog("fire pushOnDemand");

        int targetClientNumber = SentenceUtils.getClientNumber(clientSentence);
        String fileName = SentenceUtils.getFileName(clientSentence);

        ActionUtils.uploadIfFileExist(clientNumber, targetClientNumber, fileName);
    }

    private static void rePush(int clientNumber, String clientSentence) {
        Logger.clientDebugLog("fire rePush");

        int targetClientNumber = SentenceUtils.getClientNumber(clientSentence);
        String fileName = SentenceUtils.getFileName(clientSentence);
        long receivedFilePartSize = SentenceUtils.getStartByteNumber(clientSentence);

        ActionUtils.uploadIfFileExist(clientNumber,
                targetClientNumber,
                fileName,
                receivedFilePartSize);
    }
}
