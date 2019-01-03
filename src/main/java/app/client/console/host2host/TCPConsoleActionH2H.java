package app.client.console.host2host;

import app.client.console.ConsoleCommand;
import app.client.host.host2host.TCPClientH2H;
import app.client.host.multiHost.ClientCommand;
import app.config.Config;
import app.utils.ActionUtils;
import app.utils.Logger;
import app.utils.SentenceUtils;
import app.utils.TCPConnectionUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Objects;

class TCPConsoleActionH2H {

    // TODO REMEMBER connecting with second host, not server!

    // TODO NOW IMPLEMENT H2H VERSION :)
    //  and then write run scripts
    //  and update README - max file size (Inreger.MAX_VALUE - 8 bytes)

    public static void perform(TCPClientH2H client, String userSentence, int connectedHostPortNumber) {
        Logger.consoleDebugLog("perform: " + userSentence);

        userSentence = SentenceUtils.cleanUserSentence(userSentence);
        userSentence = SentenceUtils.setClientNumber(userSentence, client.getConnectedClientNumber());
        String command = SentenceUtils.getConsoleCommand(userSentence);

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
            //TODO Add close option (setting both connectedClientNumber to -1)
            case UNSUPPORTED_COMMAND:
            default:
                Logger.consoleLog("command is not supported");
                break;
        }
    }

    private static void getFileList(String command, int connectedHostPortNumber) {
        Logger.consoleDebugLog("fire getFileList");

        Socket connectionSocket = null;
        try {
            connectionSocket = new Socket(Config.HOST_IP, connectedHostPortNumber);
        } catch (IOException e) {
            e.printStackTrace();
        }

        DataOutputStream outToServer = null;
        try {
            outToServer = new DataOutputStream(Objects.requireNonNull(connectionSocket).getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Logger.consoleDebugLog(command + " output: " + "no message");
        try {
            Objects.requireNonNull(outToServer).writeBytes(command + "\n");
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
        Logger.consoleDebugLog(command + " input: " + response);

        int serverFileListSize = SentenceUtils.getListSize(response);
        for (int i = 0; i < serverFileListSize; i++) {
            try {
                Logger.consoleLog(
                        inFromServer.readLine().replaceAll("\\|", " ")
                );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            connectionSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Logger.consoleLog("Server file list was displayed");
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
            TCPConnectionUtils.sendMessageToDataOutputStream(outToClient, command, String.valueOf(clientNumber), fileName);

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
