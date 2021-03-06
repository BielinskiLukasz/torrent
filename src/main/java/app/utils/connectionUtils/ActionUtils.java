package app.utils.connectionUtils;

import app.client.console.ConsoleCommand;
import app.client.host.ClientCommand;
import app.config.Config;
import app.server.ServerCommand;
import app.utils.ExceptionHandler;
import app.utils.Logger;
import app.utils.fileUtils.FileList;
import app.utils.fileUtils.MD5Sum;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

import static java.lang.Thread.sleep;

public class ActionUtils {

    public static void uploadIfFileExist(int sourceClientNumber, int targetClientNumber, String fileName) {
        if (isClientHaveFile(sourceClientNumber, fileName)) {
            upload(sourceClientNumber, targetClientNumber, fileName);
        } else {
            Logger.consoleLog("Haven't selected file");
        }
    }

    public static void uploadIfFileExist(int sourceClientNumber,
                                         int targetClientNumber,
                                         String fileName,
                                         long receivedFilePartSize) {
        if (isClientHaveFile(sourceClientNumber, fileName)) {
            upload(sourceClientNumber, targetClientNumber, fileName, receivedFilePartSize);
        } else {
            Logger.consoleLog("You haven't selected file");
        }
    }

    private static boolean isClientHaveFile(int clientNumber, String fileName) {
        List<String> clientFileNameList = FileList.getFileNameList(clientNumber);
        return clientFileNameList.contains(fileName);
    }

    private static void upload(int sourceClientNumber, int targetClientNumber, String fileName) {
        String filePath = Config.BASIC_PATH + sourceClientNumber + "//" + fileName;
        File file = new File(filePath);

        Logger.consoleLog("Sending file " + fileName + " to client " + targetClientNumber + " started");

        Socket hostConnectionSocket = TCPConnectionUtils.createSocket(Config.HOST_IP,
                Config.PORT_NR + targetClientNumber);

        DataOutputStream outToClient = TCPConnectionUtils.getDataOutputStream(hostConnectionSocket);
        String command = String.valueOf(ClientCommand.HANDLE_PUSH);
        TCPConnectionUtils.writeMessageToDataOutputStream(outToClient,
                command,
                String.valueOf(sourceClientNumber),
                fileName);

        String md5sum = MD5Sum.md5(filePath);
        String response = "Sending file " + fileName + " md5 sum";
        TCPConnectionUtils.writeMessageToDataOutputStream(outToClient,
                command,
                String.valueOf(sourceClientNumber),
                response,
                md5sum);

        try {
            sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        FileInputStream fileInputStream = TCPConnectionUtils.createFileInputStream(file);
        OutputStream outputStream = TCPConnectionUtils.getOutputStream(hostConnectionSocket);
        TCPConnectionUtils.writeFileToStream(fileInputStream, outputStream);
        TCPConnectionUtils.closeFileInputStream(fileInputStream);

        Logger.consoleLog("Sending file " + fileName + " to client " + targetClientNumber + " ends");

        boolean reconnect = false;
        TCPConnectionUtils.closeSocket(hostConnectionSocket);

        try {
            sleep(5);
        } catch (InterruptedException e) {
            ExceptionHandler.handle(e);
        }

        for (int i = 0; i < Config.WAITING_TIME_SEC; i++) {
            try {
                Logger.clientDebugLog("Try reconnect with client " + targetClientNumber);
                hostConnectionSocket = new Socket(Config.HOST_IP, Config.PORT_NR + targetClientNumber);
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
            outToClient = TCPConnectionUtils.getDataOutputStream(hostConnectionSocket);
            command = String.valueOf(ClientCommand.CHECK_SENDING);
            TCPConnectionUtils.writeMessageToDataOutputStream(outToClient,
                    command,
                    String.valueOf(sourceClientNumber),
                    fileName,
                    MD5Sum.md5(filePath));
        } else {
            TCPConnectionUtils.closeSocket(hostConnectionSocket);

            Logger.clientLog("Cannot reconnect with client " + sourceClientNumber);
        }

        TCPConnectionUtils.closeSocket(hostConnectionSocket);

        Logger.consoleLog("Finished successfully");
    }

    private static void upload(int sourceClientNumber,
                               int targetClientNumber,
                               String fileName,
                               long receivedFilePartSize) {
        String filePath = Config.BASIC_PATH + sourceClientNumber + "//" + fileName;
        File file = new File(filePath);

        Logger.consoleLog("Resend file " + fileName + " started");

        Socket hostConnectionSocket = TCPConnectionUtils.createSocket(Config.HOST_IP,
                Config.PORT_NR + targetClientNumber);

        DataOutputStream outToClient = TCPConnectionUtils.getDataOutputStream(hostConnectionSocket);
        String command = String.valueOf(ClientCommand.HANDLE_RE_PUSH);
        String md5sum = MD5Sum.md5(filePath);

        TCPConnectionUtils.writeMessageToDataOutputStream(outToClient,
                command,
                String.valueOf(sourceClientNumber),
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

        OutputStream outputStream = TCPConnectionUtils.getOutputStream(hostConnectionSocket);
        TCPConnectionUtils.writeFileToStream(fileInputStream, outputStream);
        TCPConnectionUtils.closeFileInputStream(fileInputStream);

        Logger.consoleLog("Resend file " + fileName + " finished");

        TCPConnectionUtils.closeSocket(hostConnectionSocket);

        boolean reconnect = false;
        TCPConnectionUtils.closeSocket(hostConnectionSocket);

        for (int i = 0; i < Config.WAITING_TIME_SEC; i++) {
            try {
                Logger.clientDebugLog("Try reconnect with client " + targetClientNumber);
                hostConnectionSocket = new Socket(Config.HOST_IP, Config.PORT_NR + targetClientNumber);
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
            outToClient = TCPConnectionUtils.getDataOutputStream(hostConnectionSocket);
            command = String.valueOf(ClientCommand.CHECK_SENDING);
            TCPConnectionUtils.writeMessageToDataOutputStream(outToClient,
                    command,
                    String.valueOf(sourceClientNumber),
                    fileName,
                    MD5Sum.md5(filePath));
        } else {
            TCPConnectionUtils.closeSocket(hostConnectionSocket);

            Logger.clientLog("Cannot reconnect with client " + sourceClientNumber);
        }

        Logger.consoleLog("Finished");
    }

    public static boolean isSelectedClientConnected(int sourceClientNumber) {
        Socket connectionSocket = TCPConnectionUtils.createSocket(Config.HOST_IP, Config.PORT_NR);

        DataOutputStream outToServer = TCPConnectionUtils.getDataOutputStream(connectionSocket);
        TCPConnectionUtils.writeMessageToDataOutputStream(outToServer,
                String.valueOf(ServerCommand.CONFIRM_CONNECTION),
                String.valueOf(sourceClientNumber));

        BufferedReader inFromServer = TCPConnectionUtils.getBufferedReader(connectionSocket);
        String response = TCPConnectionUtils.readBufferedReaderLine(inFromServer);
        boolean sourceClientConnected = SentenceUtils.getBoolean(response);

        TCPConnectionUtils.closeSocket(connectionSocket);

        return sourceClientConnected;
    }

    public static void sendList(Socket connectionSocket, List serverFileList) {
        DataOutputStream outToClient = TCPConnectionUtils.getDataOutputStream(connectionSocket);
        int listSize = serverFileList.size();
        String command = String.valueOf(ConsoleCommand.FILE_LIST);
        TCPConnectionUtils.writeMessageToDataOutputStream(outToClient, command, String.valueOf(listSize));

        serverFileList.forEach(
                fileData -> TCPConnectionUtils.writeMessageToDataOutputStream(outToClient, String.valueOf(fileData))
        );
    }
}
