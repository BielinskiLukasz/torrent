package app.client.console.multiHost;

import app.client.ClientActionUtils;
import app.client.console.ConsoleCommand;
import app.client.host.ClientCommand;
import app.config.Config;
import app.server.ServerCommand;
import app.utils.ConnectionUtils;
import app.utils.ConsoleCommandUtils;
import app.utils.Logger;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.net.Socket;

public class TCPConsoleActionMH {

    public static void perform(int clientNumber, String userSentence) {
        Logger.consoleDebugLog("perform: " + userSentence);

        if (!userSentence.contains(Config.SPLITS_CHAR)) {
            userSentence = addSplitChars(userSentence);
        }
        String command = ConsoleCommandUtils.getConsoleCommand(userSentence);

        switch (ConsoleCommand.valueOf(command)) {
            case FILE_LIST:
                getFileList();
                break;
            case PULL:
                pull(clientNumber, userSentence);
                break;
            case PUSH:
                push(clientNumber, userSentence);
                break;
            case CLOSE:
                close(clientNumber);
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

    private static void getFileList() {
        Logger.consoleDebugLog("fire getFileList");

        Socket connectionSocket = ConnectionUtils.createSocket(Config.HOST_IP, Config.PORT_NR);

        DataOutputStream outToServer = ConnectionUtils.getDataOutputStream(connectionSocket);
        String command = String.valueOf(ServerCommand.SERVER_FILE_LIST);
        ConnectionUtils.sendMessageToDataOutputStream(outToServer, command);

        BufferedReader inFromServer = ConnectionUtils.getBufferedReader(connectionSocket);
        String response = ConnectionUtils.readBufferedReaderLine(inFromServer);

        int serverFileListSize = ConsoleCommandUtils.getListSize(response);
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

        int sourceClientNumber = ConsoleCommandUtils.getClientNumber(userSentence);

        if (isClientChooseHisOwnNumber(clientNumber, sourceClientNumber)) {
            Logger.consoleLog("This is not the client you are looking for :)");
            Logger.consoleLog("There is no need to download the file from yourself");
        } else {
            if (ClientActionUtils.isSelectedClientConnected(sourceClientNumber)) {

                Socket hostConnectionSocket = ConnectionUtils.createSocket(Config.HOST_IP,
                        Config.PORT_NR + sourceClientNumber);

                DataOutputStream outToClient = ConnectionUtils.getDataOutputStream(hostConnectionSocket);
                String command = String.valueOf(ClientCommand.PUSH_ON_DEMAND);
                String fileName = ConsoleCommandUtils.getFileName(userSentence);
                ConnectionUtils.sendMessageToDataOutputStream(outToClient, command, String.valueOf(clientNumber), fileName);

                ConnectionUtils.closeSocket(hostConnectionSocket);

                Logger.consoleLog("Finished");
            } else {
                Logger.consoleLog("Client " + sourceClientNumber + " isn't connected");
            }
        }
    }

    private static void push(int clientNumber, String userSentence) {
        Logger.consoleDebugLog("fire push");

        int targetClientNumber = ConsoleCommandUtils.getClientNumber(userSentence);
        String fileName = ConsoleCommandUtils.getFileName(userSentence);

        if (isClientChooseHisOwnNumber(clientNumber, targetClientNumber)) {
            Logger.consoleLog("This is not the client you are looking for :)");
            Logger.consoleLog("There is no need to upload the file to yourself");
        } else if (ClientActionUtils.isSelectedClientConnected(targetClientNumber)) {
            ClientActionUtils.uploadIfFileExist(clientNumber, targetClientNumber, fileName);
        } else {
            Logger.consoleLog("You haven't selected file");
        }
    }

    private static boolean isClientChooseHisOwnNumber(int clientNumber, int targetClientNumber) {
        return targetClientNumber == clientNumber;
    }

    private static void close(int clientNumber) {
        Logger.consoleDebugLog("fire close");

        Socket connectionSocket = ConnectionUtils.createSocket(Config.HOST_IP, Config.PORT_NR);

        DataOutputStream outToServer = ConnectionUtils.getDataOutputStream(connectionSocket);
        String command = String.valueOf(ServerCommand.UNREGISTER);
        ConnectionUtils.sendMessageToDataOutputStream(outToServer, command, String.valueOf(clientNumber));

        BufferedReader inFromServer = ConnectionUtils.getBufferedReader(connectionSocket);
        ConnectionUtils.readBufferedReaderLine(inFromServer);

        ConnectionUtils.closeSocket(connectionSocket);

        Logger.consoleLog("Connection closed");
    }
}
