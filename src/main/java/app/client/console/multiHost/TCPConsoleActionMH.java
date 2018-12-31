package app.client.console.multiHost;

import app.client.console.ConsoleCommand;
import app.config.Config;
import app.server.ServerCommand;
import app.utils.ActionUtils;
import app.utils.ConnectionUtils;
import app.utils.FileList;
import app.utils.Logger;
import app.utils.MD5Sum;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

public class TCPConsoleActionMH {

    public static void perform(int clientNumber, String userSentence) {
        if (!userSentence.contains(Config.SPLITS_CHAR)) {
            userSentence = addSplitChars(userSentence);
        }
        String command = ActionUtils.getConsoleCommand(userSentence);

        switch (ConsoleCommand.valueOf(command)) {
            case FILE_LIST:
                getFileList(command);
                break;
            case PULL:
                pull(clientNumber, userSentence); // TODO BACKLOG implement protect against file overwriting
                break;
            case PUSH:
                push(clientNumber, userSentence); // TODO BACKLOG implement protect against file overwriting
                break;
            case CLOSE:
                close(clientNumber, command);
                break;
            case EMPTY_COMMAND:
                break;
            case UNSUPPORTED_COMMAND:
            default:
                Logger.consoleLog("command is not supported");
                break;
        }
    }

    private static String addSplitChars(String userSentence) {
        for (int i = 0; i < (Config.MAX_NUMBER_OF_PARAMETERS - 1); i++) {
            userSentence = userSentence.replaceFirst(" ", Config.SPLITS_CHAR);
        }

        return userSentence;
    }

    private static void getFileList(String command) {
        Logger.consoleDebugLog("fire getFileList");

        Socket connectionSocket = ConnectionUtils.createSocket(Config.HOST_IP, Config.PORT_NR);

        DataOutputStream outToServer = ConnectionUtils.getDataOutputStream(connectionSocket);
        ConnectionUtils.sendMessageToDataOutputStream(outToServer, command);

        BufferedReader inFromServer = ConnectionUtils.getBufferedReader(connectionSocket);
        String response = ConnectionUtils.readBufferedReaderLine(inFromServer);

        int serverFileListSize = ActionUtils.getListSize(response);
        for (int i = 0; i < serverFileListSize; i++) {
            Logger.consoleLog(
                    ConnectionUtils.readBufferedReaderLine(inFromServer)
                            .replaceAll(String.format("\\%s", Config.FILE_INFO_SPLITS_CHAR), " ")
                    // TODO move getting better format to another place
            );
        }

        ConnectionUtils.closeSocket(connectionSocket);

        Logger.consoleLog("Server file list was displayed");
    }

    private static void pull(int clientNumber, String userSentence) {
        Logger.consoleDebugLog("fire pull");

        int sourceClientNumber = ActionUtils.getClientNumber(userSentence);

        if (isClientChooseHisOwnNumber(clientNumber, sourceClientNumber)) {
            Logger.consoleLog("This is not the client you are looking for :)");
            Logger.consoleLog("There is no need to download the file from yourself");
        } else {
            if (isSelectedClientConnected(sourceClientNumber)) {

                Socket hostConnectionSocket = ConnectionUtils.createSocket(Config.HOST_IP,
                        Config.PORT_NR + sourceClientNumber);

                DataOutputStream outToClient = ConnectionUtils.getDataOutputStream(hostConnectionSocket);
                String command = ActionUtils.getConsoleCommand(userSentence);
                String fileName = ActionUtils.getFileName(userSentence);
                ConnectionUtils.sendMessageToDataOutputStream(outToClient, command, String.valueOf(clientNumber), fileName);


                BufferedReader inFromClient = ConnectionUtils.getBufferedReader(hostConnectionSocket);
                String response = ConnectionUtils.readBufferedReaderLine(inFromClient);

                Boolean fileExist = ActionUtils.getBoolean(response);
                String message = ActionUtils.getMessage(response);

                Logger.consoleLog(message);
                Logger.consoleDebugLog(String.valueOf(fileExist));

                if (fileExist) {
                    response = ConnectionUtils.readBufferedReaderLine(inFromClient);
                    String fileMD5Sum = ActionUtils.getMD5Sum(response);

                    String filePath = Config.BASIC_PATH + clientNumber + "//" + fileName;
                    File file = new File(filePath);
                    FileOutputStream fileOutputStream = ConnectionUtils.createFileOutputStream(file);
                    InputStream inputStream = ConnectionUtils.getInputStream(hostConnectionSocket);
                    ConnectionUtils.readFileFromStream(fileOutputStream, inputStream);
                    ConnectionUtils.closeFileOutputStream(fileOutputStream);

                    if (MD5Sum.check(filePath, fileMD5Sum)) {
                        Logger.consoleLog("File downloaded successfully");
                    } else {
                        Logger.consoleLog("Unsuccessful file download");
                        if (file.delete()) {
                            Logger.consoleDebugLog("Removed invalid file");
                        }
                    }
                }

                ConnectionUtils.closeSocket(hostConnectionSocket);

                Logger.consoleLog("Finished");
            } else {
                Logger.consoleLog("Client " + sourceClientNumber + " isn't connected");
            }
        }
    }

    private static void push(int clientNumber, String userSentence) {
        Logger.consoleDebugLog("fire push");

        int targetClientNumber = ActionUtils.getClientNumber(userSentence);
        String fileName = ActionUtils.getFileName(userSentence);

        if (isClientChooseHisOwnNumber(clientNumber, targetClientNumber)) {
            Logger.consoleLog("This is not the client you are looking for :)");
            Logger.consoleLog("There is no need to upload the file to yourself");
        } else if (!isClientHaveFile(clientNumber, fileName)) {
            Logger.consoleLog("You haven't selected file");
        } else {
            if (isSelectedClientConnected(targetClientNumber)) {
                String filePath = Config.BASIC_PATH + clientNumber + "//" + fileName;
                File file = new File(filePath);

                Socket hostConnectionSocket = ConnectionUtils.createSocket(Config.HOST_IP,
                        Config.PORT_NR + targetClientNumber);

                DataOutputStream outToClient = ConnectionUtils.getDataOutputStream(hostConnectionSocket);
                String command = ActionUtils.getConsoleCommand(userSentence);
                ConnectionUtils.sendMessageToDataOutputStream(outToClient, command, String.valueOf(clientNumber), fileName);

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
            } else {
                Logger.consoleLog("Client " + targetClientNumber + " isn't connected");
            }
        }

    }

    private static boolean isClientChooseHisOwnNumber(int clientNumber, int targetClientNumber) {
        return targetClientNumber == clientNumber;
    }

    private static boolean isSelectedClientConnected(int sourceClientNumber) {
        Socket connectionSocket = ConnectionUtils.createSocket(Config.HOST_IP, Config.PORT_NR);

        DataOutputStream outToServer = ConnectionUtils.getDataOutputStream(connectionSocket);
        ConnectionUtils.sendMessageToDataOutputStream(outToServer,
                String.valueOf(ServerCommand.CONFIRM_CONNECTION),
                String.valueOf(sourceClientNumber));

        BufferedReader inFromServer = ConnectionUtils.getBufferedReader(connectionSocket);
        String response = ConnectionUtils.readBufferedReaderLine(inFromServer);
        boolean sourceClientConnected = ActionUtils.getBoolean(response);

        ConnectionUtils.closeSocket(connectionSocket);
        return sourceClientConnected;
    }

    private static boolean isClientHaveFile(int clientNumber, String fileName) {
        List<String> clientFileNameList = FileList.getFileNameList(clientNumber);
        return clientFileNameList.contains(fileName);
    }

    private static void close(int clientNumber, String command) {
        Logger.consoleDebugLog("fire close");

        Socket connectionSocket = ConnectionUtils.createSocket(Config.HOST_IP, Config.PORT_NR);

        DataOutputStream outToServer = ConnectionUtils.getDataOutputStream(connectionSocket);
        ConnectionUtils.sendMessageToDataOutputStream(outToServer, command, String.valueOf(clientNumber));

        BufferedReader inFromServer = ConnectionUtils.getBufferedReader(connectionSocket);
        ConnectionUtils.readBufferedReaderLine(inFromServer);

        ConnectionUtils.closeSocket(connectionSocket);

        Logger.consoleLog("Connection closed");
    }
}
