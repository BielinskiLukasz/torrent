package app.client.console.host2host;

import app.client.console.ConsoleCommand;
import app.config.Config;
import app.utils.ActionUtils;
import app.utils.Logger;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class TCPConsoleActionH2H {

    public static void perform(int clientNumber, String userSentence, int connectedHostPortNumber) {
        String command = getCommandAppName(ActionUtils.getCommand(userSentence));

        switch (ConsoleCommand.valueOf(command)) {
            case FILE_LIST:
                getFileList(command, connectedHostPortNumber);
                break;
            case PULL:
                pull(clientNumber, userSentence, connectedHostPortNumber); // TODO remove sourceClientNumber from message
                break;
            case EMPTY_COMMAND:
                break;
            case UNSUPPORTED_COMMAND:
            default:
                Logger.appLog("command is not supported");
                break;
        }
    }

    private static String getCommandAppName(String command) {
        switch (command.trim().toUpperCase()) {
            case "L":
            case "LIST":
            case "FILES":
            case "FL":
            case "FILE_LIST":
            case "FILES LIST":
            case "FILESLIST":
            case "FILES_LIST":
            case "FILE LIST":
            case "FILELIST":
                return ConsoleCommand.FILE_LIST.name();
            case "C":
            case "CLOSE":
            case "E":
            case "EXIT":
            case "Q":
            case "QUIT":
                return ConsoleCommand.UNSUPPORTED_COMMAND.name();
            case "PULL":
            case "D":
            case "DOWNLOAD":
                return ConsoleCommand.PULL.name();
            case "PUSH":
            case "U":
            case "UPLOAD":
                return ConsoleCommand.PUSH.name();
            case "":
                return ConsoleCommand.EMPTY_COMMAND.name();
            default:
                return ConsoleCommand.UNSUPPORTED_COMMAND.name();
        }
    }

    private static void getFileList(String command, int connectedHostPortNumber) {
        Logger.appDebugLog("fire getFileList");

        Socket connectionSocket = null;
        try {
            connectionSocket = new Socket(Config.HOST_IP, connectedHostPortNumber);
        } catch (IOException e) {
            e.printStackTrace();
        }

        DataOutputStream outToServer = null;
        try {
            outToServer = new DataOutputStream(connectionSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Logger.appDebugLog(command + " output: " + "no message");
        try {
            outToServer.writeBytes(command + "\n");
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
            response = inFromServer.readLine();
        } catch (IOException e) {
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
                e.printStackTrace();
            }
        }

        try {
            connectionSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Logger.appLog("Server file list was displayed");
    }

    private static void pull(int clientNumber, String userSentence, int connectedHostPortNumber) {
        Logger.appDebugLog("fire pull");

        int sourceClientNumber = ActionUtils.getClientNumber(userSentence);

        if (sourceClientNumber == clientNumber) {
            Logger.appLog("It is not the customer you are looking for :)");
            Logger.appLog("There is no need to download the file from yourself");
        } else {

            Socket connectionSocket = null;
            try {
                connectionSocket = new Socket(Config.HOST_IP, Config.PORT_NR + connectedHostPortNumber);
            } catch (IOException e) {
                e.printStackTrace();
            }

            DataOutputStream outToClient = null;
            try {
                outToClient = new DataOutputStream(connectionSocket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            String command = ActionUtils.getCommand(userSentence);
            String fileName = ActionUtils.getFileName(userSentence);

            Logger.appDebugLog(command + " output: " + clientNumber + " " + fileName);
            try {
                outToClient.writeBytes(command + Config.SENTENCE_SPLITS_CHAR + clientNumber +
                        Config.SENTENCE_SPLITS_CHAR + fileName + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }

            InputStream inputStream = null;
            try {
                inputStream = connectionSocket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            BufferedReader inFromClient = null;
            try {
                inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }

            String response = null;
            try {
                response = inFromClient.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Logger.appDebugLog(command + " input: " + response);

            Logger.appLog(response);

            File file = new File(Config.BASIC_PATH + clientNumber + "//" + fileName);
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Logger.appDebugLog(command + " open fileOutputStream");

            int count;
            byte[] buffer = new byte[8192];
            try {
                while ((count = inputStream.read(buffer)) > 0) {
                    fileOutputStream.write(buffer, 0, count);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Logger.appDebugLog(command + " close fileOutputStream");

            // TODO implements checking md5 sum (and delete file if aren't correct)

            try {
                connectionSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Logger.appLog("Finish sending file");
        }
    }
}
