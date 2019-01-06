package app.client.console.host2host;

import app.client.console.ConsoleCommand;
import app.client.host.ClientCommand;
import app.client.host.host2host.TCPClientH2H;
import app.config.Config;
import app.utils.Logger;
import app.utils.connectionUtils.ActionUtils;
import app.utils.connectionUtils.CommandUtils;
import app.utils.connectionUtils.SentenceUtils;
import app.utils.connectionUtils.TCPConnectionUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.net.Socket;

class TCPConsoleActionH2H {

    public static void perform(TCPClientH2H client, String userSentence, int connectedHostPortNumber) {
        Logger.consoleDebugLog("perform: " + userSentence);

        userSentence = SentenceUtils.cleanUserSentence(userSentence);
        userSentence = SentenceUtils.setClientNumber(userSentence, client.getConnectedClientNumber());
        String command = CommandUtils.getConsoleCommand(userSentence);

        Logger.consoleDebugLog("perform: " + userSentence);

        switch (ConsoleCommand.valueOf(command)) {
            case FILE_LIST:
                getFileList(command, connectedHostPortNumber);
                break;
            case PUSH:
                push(client.getClientNumber(), userSentence);
                break;
            case PULL:
                pull(client.getClientNumber(), userSentence);
                break;
            case EMPTY_COMMAND:
                break;
            case UNSUPPORTED_COMMAND:
            default:
                Logger.consoleLog("command is not supported");
                break;
        }
    }

    private static void getFileList(String command, int connectedHostPortNumber) {
        Logger.consoleDebugLog("fire getFileList");

        Socket connectionSocket = TCPConnectionUtils.createSocket(Config.HOST_IP, connectedHostPortNumber);

        DataOutputStream outToServer = TCPConnectionUtils.getDataOutputStream(connectionSocket);
        TCPConnectionUtils.writeMessageToDataOutputStream(outToServer, command);

        BufferedReader inFromServer = TCPConnectionUtils.getBufferedReader(connectionSocket);
        String response = TCPConnectionUtils.readBufferedReaderLine(inFromServer);

        int serverFileListSize = SentenceUtils.getListSize(response);
        for (int i = 0; i < serverFileListSize; i++) {
            Logger.consoleLog(
                    TCPConnectionUtils.readBufferedReaderLine(inFromServer)
                            .replaceAll(String.format("\\%s", Config.FILE_INFO_SPLITS_CHAR), " ")
            );
        }

        TCPConnectionUtils.closeSocket(connectionSocket);

        Logger.consoleLog("File list was displayed");
    }

    private static void pull(int clientNumber, String userSentence) {
        Logger.consoleDebugLog("fire pull");

        int sourceClientNumber = SentenceUtils.getClientNumber(userSentence);

        if (isClientChooseHisOwnNumber(clientNumber, sourceClientNumber)) {
            Logger.consoleLog("This is not the client you are looking for :)");
            Logger.consoleLog("There is no need to download the file from yourself");
        } else {
            Socket hostConnectionSocket = TCPConnectionUtils.createSocket(Config.HOST_IP,
                    Config.PORT_NR + sourceClientNumber);

            DataOutputStream outToClient = TCPConnectionUtils.getDataOutputStream(hostConnectionSocket);
            String command = String.valueOf(ClientCommand.PUSH_ON_DEMAND);
            String fileName = SentenceUtils.getFileName(userSentence);
            TCPConnectionUtils.writeMessageToDataOutputStream(outToClient,
                    command,
                    String.valueOf(clientNumber),
                    fileName);

            TCPConnectionUtils.closeSocket(hostConnectionSocket);

            Logger.consoleLog("Sending push request");
            Logger.consoleLog("Finished");
        }
    }

    private static void push(int clientNumber, String userSentence) {
        Logger.consoleDebugLog("fire push");

        int targetClientNumber = SentenceUtils.getClientNumber(userSentence);
        String fileName = SentenceUtils.getFileName(userSentence);

        if (isClientChooseHisOwnNumber(clientNumber, targetClientNumber)) {
            Logger.consoleLog("This is not the client you are looking for :)");
            Logger.consoleLog("There is no need to upload the file to yourself");
        } else {
            ActionUtils.uploadIfFileExist(clientNumber, targetClientNumber, fileName);
        }
    }

    private static boolean isClientChooseHisOwnNumber(int clientNumber, int targetClientNumber) {
        return targetClientNumber == clientNumber;
    }
}
