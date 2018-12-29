package app.client.console;

import app.Utils.ActionUtils;
import app.Utils.Logger;
import app.config.Config;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

class TCPAppAction {

    static void perform(int clientNumber, String userSentence) {
        String command = getCommandAppName(ActionUtils.getCommand(userSentence));

        switch (CommandApp.valueOf(command)) {
            case FILE_LIST:
                getFileList(command);
                break;
            case CLOSE:
                close(clientNumber, command);
                break;
            case EMPTY_COMMAND:
                break;
            case UNSUPPORTED_COMMAND:
            default:
                Logger.appLog("command is not supported");
                break;
        }
    }

    private static void getFileList(String command) {
        Logger.appDebugLog("fire getFileList");

        Socket connectionSocket = null;
        try {
            connectionSocket = new Socket(Config.HOST_IP, Config.PORT_NR);
        } catch (IOException e) {
            System.out.println("TCPAppAction - creating socket " + e);
            e.printStackTrace();
        }

        DataOutputStream outToServer = null;
        try {
            outToServer = new DataOutputStream(connectionSocket.getOutputStream());
        } catch (IOException e) {
            System.out.println("TCPAppAction - creating dataOutputStream " + e);
            e.printStackTrace();
        }

        Logger.appDebugLog(command + " output: " + "no message");
        try {
            outToServer.writeBytes(command + "\n");
        } catch (IOException e) {
            System.out.println("TCPAppAction - write to server " + e);
            e.printStackTrace();
        }

        BufferedReader inFromServer = null;
        try {
            inFromServer = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        } catch (IOException e) {
            System.out.println("TCPAppAction - creating inputBufferedReader " + e);
            e.printStackTrace();
        }

        String response = null;
        try {
            response = inFromServer.readLine();
        } catch (IOException e) {
            System.out.println("TCPAppAction - read from server " + e);
            e.printStackTrace();
        }
        Logger.appDebugLog(command + " input: " + response);

        int serverFileListSize = ActionUtils.getListSize(response);
        for (int i = 0; i < serverFileListSize; i++) {
            try {
                Logger.appLog(
                        inFromServer.readLine().replaceAll("\\|", " ")
                );
            } catch (IOException e) {
                System.out.println("TCPServerAction - read from client (specific clientFile) " + e);
                e.printStackTrace();
            }
        }

        try {
            connectionSocket.close();
        } catch (IOException e) {
            System.out.println("TCPAppAction - closing socket " + e);
            e.printStackTrace();
        }

        Logger.appLog("Server file list was displayed");
    }

    private static void close(int clientNumber, String command) {
        Logger.appDebugLog("fire close");

        Socket connectionSocket = null;
        try {
            connectionSocket = new Socket(Config.HOST_IP, Config.PORT_NR);
        } catch (IOException e) {
            System.out.println("TCPAppAction - creating socket " + e);
            e.printStackTrace();
        }

        DataOutputStream outToServer = null;
        try {
            outToServer = new DataOutputStream(connectionSocket.getOutputStream());
        } catch (IOException e) {
            System.out.println("TCPAppAction - creating dataOutputStream " + e);
            e.printStackTrace();
        }

        Logger.appDebugLog(command + " output: " + clientNumber);
        try {
            outToServer.writeBytes(command + Config.SENTENCE_SPLITS_CHAR + clientNumber + "\n");
        } catch (IOException e) {
            System.out.println("TCPAppAction - write to server " + e);
            e.printStackTrace();
        }

        BufferedReader inFromServer = null;
        try {
            inFromServer = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        } catch (IOException e) {
            System.out.println("TCPAppAction - creating inputBufferedReader " + e);
            e.printStackTrace();
        }

        String response = null;
        try {
            response = inFromServer.readLine();
        } catch (IOException e) {
            System.out.println("TCPAppAction - read from server " + e);
            e.printStackTrace();
        }
        Logger.appDebugLog(command + " input: " + response);

        try {
            connectionSocket.close();
        } catch (IOException e) {
            System.out.println("TCPAppAction - closing socket " + e);
            e.printStackTrace();
        }

        Logger.appLog("Connection closed");
    }

    private static String getCommandAppName(String command) {
        switch (command.trim().toUpperCase()) {
            case "L":
            case "LIST":
            case "FILES":
            case "FILE_LIST":
            case "FILES LIST":
            case "FILESLIST":
            case "FILES_LIST":
            case "FILE LIST":
            case "FILELIST":
                return CommandApp.FILE_LIST.name();

            case "CLOSE":
            case "EXIT":
            case "QUIT":
                return CommandApp.CLOSE.name();

            case "":
                return CommandApp.EMPTY_COMMAND.name();

            default:
                return CommandApp.UNSUPPORTED_COMMAND.name();
        }
    }
}
