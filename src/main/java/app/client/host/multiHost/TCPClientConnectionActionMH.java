package app.client.host.multiHost;

import app.client.console.ConsoleCommand;
import app.client.host.ClientCommand;
import app.config.Config;
import app.server.ServerCommand;
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
import java.io.RandomAccessFile;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

public class TCPClientConnectionActionMH {

    public static void perform(int clientNumber, Socket connectionSocket, String sentence) {
        Logger.clientDebugLog("perform: " + sentence);

        String command = CommandUtils.getCommand(sentence);

        switch (ClientCommand.valueOf(command)) {
            case CONNECT:
                connect(clientNumber, connectionSocket, sentence);
                break;
            case CLIENT_FILE_LIST:
                getClientFileList(clientNumber, connectionSocket, sentence);
                break;
            case CLIENT_FILE_INFO:
                getFileInfo(clientNumber, connectionSocket, sentence);
                break;
            case HANDLE_PUSH:
                handlePush(clientNumber, connectionSocket, sentence);
                break;
            case HANDLE_PUSH_PACK:
                handlePushPack(clientNumber, connectionSocket, sentence);
                break;
            case PUSH_ON_DEMAND:
                pushOnDemand(clientNumber, sentence);
                break;
            case REPUSH:
                repush(clientNumber, sentence);
                break;
            case HANDLE_REPUSH:
                handleRepush(clientNumber, connectionSocket, sentence);
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
        TCPConnectionUtils.writeMessageToDataOutputStream(outToServer, command, String.valueOf(clientNumber), message);
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

    private static void getFileInfo(int clientNumber, Socket connectionSocket, String clientSentence) {
        Logger.clientDebugLog("fire getFileInfo");

        String command = SentenceUtils.getCommand(clientSentence);
        Logger.clientDebugLog(command + " input: " + clientSentence);

        String fileName = SentenceUtils.getFileName(clientSentence);
        String filePath = Config.BASIC_PATH + clientNumber + "//" + fileName;
        File file = new File(filePath);
        Long fileSize = file.length();

        DataOutputStream outToClient = TCPConnectionUtils.getDataOutputStream(connectionSocket);
        TCPConnectionUtils.writeMessageToDataOutputStream(outToClient,
                String.valueOf(ConsoleCommand.MULTIPLE_PULL),
                String.valueOf(clientNumber),
                fileName,
                String.valueOf(fileSize));

        Logger.clientLog("Client file info sent");
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
            invokeRepush(clientNumber, connectionSocket, clientSentence, 0);
        }

        Logger.clientDebugLog(command + " downloading sequence ended");
    }

    private static void handlePushPack(int clientNumber, Socket connectionSocket, String clientSentence) {
        Logger.clientDebugLog("fire handlePushPack");

        int targetClientNumber = SentenceUtils.getClientNumber(clientSentence);
        String fileName = SentenceUtils.getFileName(clientSentence);
        int packetNumber = SentenceUtils.getPacketNumber(clientSentence);
        long startByteNum = SentenceUtils.getStartByteNumber(clientSentence);
        long endByteNum = SentenceUtils.getEndByteNumber(clientSentence);
        Logger.clientDebugLog("file " + fileName + " start at " + startByteNum + " end at " + endByteNum);

        String filePath = Config.BASIC_PATH + clientNumber + "//" + fileName;
        int packetSize = (int) (endByteNum - startByteNum + 1);
        Logger.clientDebugLog("packet size is " + packetSize);

        List<String> parts = splitFile(fileName, packetSize, clientNumber);

        String selectedPartPath = parts.get(packetNumber);
        File file = new File(selectedPartPath);

        /*DataOutputStream outToClient = TCPConnectionUtils.getDataOutputStream(connectionSocket);
        String command = String.valueOf(ConsoleCommand.MULTIPLE_PULL);
        String md5sum = MD5Sum.md5(selectedPartPath);
        String response = "Sending file " + fileName + " md5 sum";
        TCPConnectionUtils.writeMessageToDataOutputStream(outToClient,
                command,
                String.valueOf(clientNumber),
                response,
                md5sum);

        FileInputStream fileInputStream = TCPConnectionUtils.createFileInputStream(file);
        OutputStream outputStream = TCPConnectionUtils.getOutputStream(connectionSocket);
        TCPConnectionUtils.writeFileToStream(fileInputStream, outputStream);
        TCPConnectionUtils.closeFileInputStream(fileInputStream);

        Logger.consoleLog("Sending file parts " + fileName + " " + packetNumber + " to client " + targetClientNumber +
                " ends");*/











    }

    private static List<String> splitFile(final String fileName, final int packetSize, int clientNumber) {

        if (packetSize <= 0) {
            throw new IllegalArgumentException("mBperSplit must be more than zero");
        }

        List<String> partFiles = new ArrayList<>();
        String filePath = Config.BASIC_PATH + clientNumber + "//" + fileName;
        long sourceSize = 0;
        try {
            sourceSize = Files.size(Paths.get(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        final long bytesPerSplit = packetSize;
        final long numSplits = sourceSize / bytesPerSplit;
        final long remainingBytes = sourceSize % bytesPerSplit;
        int position = 0;
        int partNumber = 1;

        try (RandomAccessFile sourceFile = new RandomAccessFile(filePath, "r");
             FileChannel sourceChannel = sourceFile.getChannel()) {

            for (; position < numSplits; position++) {
                writePartToFile(fileName,
                        clientNumber,
                        partNumber++,
                        bytesPerSplit,
                        position * bytesPerSplit,
                        sourceChannel,
                        partFiles);
            }

            if (remainingBytes > 0) {
                writePartToFile(fileName,
                        clientNumber,
                        partNumber,
                        remainingBytes,
                        position * bytesPerSplit,
                        sourceChannel,
                        partFiles);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return partFiles;
    }

    private static void writePartToFile(String fileName,
                                        int clientNumber,
                                        int partNumber,
                                        long byteSize,
                                        long position,
                                        FileChannel sourceChannel,
                                        List<String> partFiles) throws IOException {
        String filePath = Config.BASIC_PATH + clientNumber + "//" + fileName;
        String basicTargetPath = Config.BASIC_PATH + clientNumber + "//" + fileName;
        String suffix = ".part_";
        String targetPath = basicTargetPath + suffix + partNumber;
        Path partFilePath = Paths.get(targetPath);

        try (RandomAccessFile toFile = new RandomAccessFile(partFilePath.toFile(), "rw");
             FileChannel toChannel = toFile.getChannel()) {
            sourceChannel.position(position);
            toChannel.transferFrom(sourceChannel, 0, byteSize);
        }
        partFiles.add(targetPath);
    }

    private static void handleRepush(int clientNumber, Socket connectionSocket, String clientSentence) {
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
            invokeRepush(clientNumber, connectionSocket, clientSentence, 0);
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
            TCPConnectionUtils.writeMessageToDataOutputStream(outToClient,
                    command,
                    String.valueOf(clientNumber),
                    fileName,
                    String.valueOf((new File(filePath)).length()));
            Logger.clientDebugLog("Repush request sended");

            TCPConnectionUtils.closeSocket(connectionSocket);
        }
    }

    private static void invokeRepush(int clientNumber,
                                     Socket connectionSocket,
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

    private static void repush(int clientNumber, String clientSentence) {
        Logger.clientDebugLog("fire repush");

        int targetClientNumber = SentenceUtils.getClientNumber(clientSentence);
        String fileName = SentenceUtils.getFileName(clientSentence);
        long receivedFilePartSize = SentenceUtils.getStartByteNumber(clientSentence);

        ActionUtils.uploadIfFileExist(clientNumber,
                targetClientNumber,
                fileName,
                receivedFilePartSize);
    }
}
