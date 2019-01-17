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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
            case RE_PUSH:
                rePush(clientNumber, sentence);
                break;
            case RE_PULL:
                rePull(clientNumber, connectionSocket, sentence);
                break;
            case HANDLE_RE_PUSH:
                handleRePush(clientNumber, connectionSocket, sentence);
                break;
            case CHECK_SENDING:
                checkingSendingCorrectness(clientNumber, sentence);
                break;
            case CREATE_PART_OF_FILE:
                createPartOfFile(clientNumber, sentence);
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

        Logger.clientDebugLog("Client file list sent to server");
    }

    private static void getFileInfo(int clientNumber, Socket connectionSocket, String clientSentence) {
        Logger.clientDebugLog("fire getFileInfo");

        String command = SentenceUtils.getCommand(clientSentence);
        Logger.clientDebugLog(command + " input: " + clientSentence);

        String fileName = SentenceUtils.getFileName(clientSentence);
        String filePath = Config.BASIC_PATH + clientNumber + "//" + fileName;
        File file = new File(filePath);
        Long fileSize = file.length();
        String md5Sum = MD5Sum.md5(filePath);

        DataOutputStream outToClient = TCPConnectionUtils.getDataOutputStream(connectionSocket);
        TCPConnectionUtils.writeMessageToDataOutputStream(outToClient,
                String.valueOf(ConsoleCommand.MULTIPLE_PULL),
                String.valueOf(clientNumber),
                fileName,
                md5Sum,
                String.valueOf(fileSize));

        Logger.clientDebugLog("Client file info sent");
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

        String selectedPartPath = getFilePart(fileName, String.valueOf(packetNumber), startByteNum, endByteNum, clientNumber);
        File file = new File(selectedPartPath);

        DataOutputStream outToClient = TCPConnectionUtils.getDataOutputStream(connectionSocket);
        String command = String.valueOf(ConsoleCommand.MULTIPLE_PULL);
        String md5sum = MD5Sum.md5(selectedPartPath);
        String response = "Sending file " + fileName + " md5 sum";
        TCPConnectionUtils.writeMessageToDataOutputStream(outToClient,
                command,
                String.valueOf(clientNumber),
                response,
                md5sum);

        try {
            sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        FileInputStream fileInputStream = TCPConnectionUtils.createFileInputStream(file);
        OutputStream outputStream = TCPConnectionUtils.getOutputStream(connectionSocket);
        TCPConnectionUtils.writeFileToStream(fileInputStream, outputStream);
        TCPConnectionUtils.closeFileInputStream(fileInputStream);

        TCPConnectionUtils.closeSocket(connectionSocket);

        File partFile = new File(filePath + ".part_" + packetNumber);
        if (partFile.delete())
            Logger.clientDebugLog("Remove part " + packetNumber);

        Logger.consoleLog("Sending file parts " + fileName + " " + packetNumber + " to client " + targetClientNumber +
                " ends");
    }

    private static String getFilePart(final String fileName,
                                      final String packetNumber,
                                      final long startByteNum,
                                      long endByteNum,
                                      int clientNumber) {
        if (endByteNum <= startByteNum) {
            throw new IllegalArgumentException("size must be more than zero");
        }

        String selectedPartPath = null;
        String filePath = Config.BASIC_PATH + clientNumber + "//" + fileName;
        long sourceSize = 0;
        try {
            sourceSize = Files.size(Paths.get(filePath));
        } catch (IOException e) {
            ExceptionHandler.handle(e);
        }

        try (RandomAccessFile sourceFile = new RandomAccessFile(filePath, "r");
             FileChannel sourceChannel = sourceFile.getChannel()) {

            if (sourceSize > endByteNum) {
                selectedPartPath = writePartToFile(fileName,
                        clientNumber,
                        packetNumber,
                        endByteNum - startByteNum + 1,
                        startByteNum,
                        sourceChannel
                );
                Logger.clientDebugLog("Create part " + packetNumber);
            } else {
                endByteNum = (int) sourceSize;
                selectedPartPath = writePartToFile(fileName,
                        clientNumber,
                        packetNumber,
                        endByteNum - startByteNum + 1,
                        startByteNum,
                        sourceChannel
                );
                Logger.clientDebugLog("Create last part " + packetNumber);
            }
        } catch (IOException e) {
            ExceptionHandler.handle(e);
        }

        return selectedPartPath;
    }

    private static String writePartToFile(String fileName,
                                          int clientNumber,
                                          String packetNumber,
                                          long byteSize,
                                          long position,
                                          FileChannel sourceChannel) throws IOException {
        String basicTargetPath = Config.BASIC_PATH + clientNumber + "//" + fileName;
        String suffix = ".part_";
        String targetPath = basicTargetPath + suffix + packetNumber;
        Path partFilePath = Paths.get(targetPath);

        try (RandomAccessFile toFile = new RandomAccessFile(partFilePath.toFile(), "rw");
             FileChannel toChannel = toFile.getChannel()) {
            sourceChannel.position(position);
            toChannel.transferFrom(sourceChannel, 0, byteSize);
            Logger.clientDebugLog("Send " + byteSize + " bytes to part " + packetNumber);
        }

        return targetPath;
    }

    private static void handleRePush(int clientNumber, Socket connectionSocket, String clientSentence) {
        Logger.clientDebugLog("fire handlePush");

        String command = SentenceUtils.getCommand(clientSentence);
        int sourceClientNumber = SentenceUtils.getClientNumber(clientSentence);
        String fileName = SentenceUtils.getFileName(clientSentence);
        Logger.clientLog("Receiving file " + fileName + " from client " + sourceClientNumber);

//        BufferedReader inFromClient = TCPConnectionUtils.getBufferedReader(connectionSocket);
//        String sentence = TCPConnectionUtils.readBufferedReaderLine(inFromClient);
        String fileMD5Sum = SentenceUtils.getMD5Sum(clientSentence);

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
            Logger.clientDebugLog("RePush request sent");

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

            try {
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
            Logger.clientDebugLog("RePush request sent");

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

    private static void rePull(int clientNumber, Socket connectionSocket, String sentence) {
        Logger.clientDebugLog("fire rePull");

        String fileName = SentenceUtils.getFileName(sentence);
        long receivedFilePartSize = SentenceUtils.getStartByteNumber(sentence);

        String filePath = Config.BASIC_PATH + clientNumber + "//" + fileName;
        File file = new File(filePath);

        Logger.clientLog("Resend file " + fileName + " started");

        DataOutputStream outToClient = TCPConnectionUtils.getDataOutputStream(connectionSocket);
        String command = String.valueOf(ClientCommand.HANDLE_RE_PUSH);
        String md5sum = MD5Sum.md5(filePath);
        TCPConnectionUtils.writeMessageToDataOutputStream(outToClient,
                command,
                String.valueOf(clientNumber),
                fileName,
                md5sum);

        FileInputStream fileInputStream = TCPConnectionUtils.createFileInputStream(file);

        try {
            Logger.clientDebugLog("Skip " + receivedFilePartSize + " bytes");
            fileInputStream.skip(receivedFilePartSize);
        } catch (IOException e) {
            ExceptionHandler.handle(e);
        }

        try {
            sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        OutputStream outputStream = TCPConnectionUtils.getOutputStream(connectionSocket);
        TCPConnectionUtils.writeFileToStream(fileInputStream, outputStream);
        TCPConnectionUtils.closeFileInputStream(fileInputStream);

        Logger.consoleLog("Resend file " + fileName + " finished");

        TCPConnectionUtils.closeSocket(connectionSocket);

        if (file.delete()) {
            Logger.consoleDebugLog("Part deleted");
        }

        Logger.consoleLog("Finished");
    }

    private static void createPartOfFile(int clientNumber, String sentence) {
        Logger.clientDebugLog("fire createPartOfFile");

        String fileName = SentenceUtils.getFileName(sentence);
        long startByteNum = SentenceUtils.getStartByteNumber(sentence);
        long endByteNum = SentenceUtils.getEndByteNumber(sentence);
        int packetNumber = SentenceUtils.getPacketNumber(sentence);

        getFilePart(fileName, String.valueOf(packetNumber), startByteNum, endByteNum, clientNumber);
    }
}
