package app.client.console.multiHost;

import app.Utils.ActionUtils;
import app.Utils.Logger;
import app.client.console.ConsoleCommand;
import app.config.Config;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class TCPConsoleActionMH {

    public static void perform(int clientNumber, String userSentence) {
        String command = getCommandAppName(ActionUtils.getCommand(userSentence));

        switch (ConsoleCommand.valueOf(command)) {
            case FILE_LIST:
                getFileList(command);
                break;
            case PULL:
                pull(clientNumber, userSentence); // TODO implement checking client connection with server!
                //                                      and protect against file overwriting
                //                                      and downloading the file from yourself
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

    private static void pull(int clientNumber, String userSentence) {
        Logger.appDebugLog("fire pull");

        int sourceClientNumber = ActionUtils.getClientNumber(userSentence);

        if (sourceClientNumber == clientNumber) {
            Logger.appLog("It is not the customer you are looking for :)");
            Logger.appLog("There is no need to download the file from yourself");
        } else {

            Socket connectionSocket = null;
            try {
                connectionSocket = new Socket(Config.HOST_IP, Config.PORT_NR + sourceClientNumber);
            } catch (IOException e) {
                System.out.println("TCPConsoleActionMH - creating socket " + e);
                e.printStackTrace();
            }

            DataOutputStream outToClient = null;
            try {
                outToClient = new DataOutputStream(connectionSocket.getOutputStream());
            } catch (IOException e) {
                System.out.println("TCPConsoleActionMH - creating dataOutputStream " + e);
                e.printStackTrace();
            }

            String command = ActionUtils.getCommand(userSentence);
            String fileName = ActionUtils.getFileName(userSentence);

            Logger.appDebugLog(command + " output: " + clientNumber + " " + fileName);
            try {
                outToClient.writeBytes(command + Config.SENTENCE_SPLITS_CHAR + clientNumber +
                        Config.SENTENCE_SPLITS_CHAR + fileName + "\n");
            } catch (IOException e) {
                System.out.println("TCPConsoleActionMH - write to server " + e);
                e.printStackTrace();
            }

            InputStream inputStream = null;
            try {
                inputStream = connectionSocket.getInputStream();
            } catch (IOException e) {
                System.out.println("TCPConsoleActionMH - creating inputStream " + e);
                e.printStackTrace();
            }

            BufferedReader inFromClient = null;
            try {
                inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            } catch (IOException e) {
                System.out.println("TCPConsoleActionMH - creating inputBufferedReader " + e);
                e.printStackTrace();
            }

            String response = null;
            try {
                response = inFromClient.readLine();
            } catch (IOException e) {
                System.out.println("TCPConsoleActionMH - read from server " + e);
                e.printStackTrace();
            }
            Logger.appDebugLog(command + " input: " + response);

            Logger.appLog(response);

            // TODO implement getting file
            File file = new File(Config.BASIC_PATH + clientNumber + "//" + fileName);
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                // TODO remove sout in catch brackets or add another one here
                e.printStackTrace();
            }
            Logger.appDebugLog(command + " open fileOutputStream");

            int count;
            byte[] buffer = new byte[8192];
            try {
                while ((count = inputStream.read(buffer)) > 0) {
                    Logger.appDebugLog(command + " reading file - count " + count);
                    fileOutputStream.write(buffer, 0, count);
                    Logger.appDebugLog(command + " read file - count " + count);
                }
            } catch (IOException e) {
                // TODO remove sout in catch brackets or add another one here
                e.printStackTrace();
            }

            try {
                fileOutputStream.close();
            } catch (IOException e) {
                // TODO remove sout in catch brackets or add another one here
                e.printStackTrace();
            }
            Logger.appDebugLog(command + " close fileOutputStream");

            // TODO implements checking md5 sum (and delete file if aren't correct)

            try {
                connectionSocket.close();
            } catch (IOException e) {
                System.out.println("TCPConsoleActionMH - closing socket " + e);
                e.printStackTrace();
            }

            Logger.appLog("Finish sending file");
        }
    }

    private static void getFileList(String command) {
        Logger.appDebugLog("fire getFileList");

        Socket connectionSocket = null;
        try {
            connectionSocket = new Socket(Config.HOST_IP, Config.PORT_NR);
        } catch (IOException e) {
            System.out.println("TCPConsoleActionMH - creating socket " + e);
            e.printStackTrace();
        }

        DataOutputStream outToServer = null;
        try {
            outToServer = new DataOutputStream(connectionSocket.getOutputStream());
        } catch (IOException e) {
            System.out.println("TCPConsoleActionMH - creating dataOutputStream " + e);
            e.printStackTrace();
        }

        Logger.appDebugLog(command + " output: " + "no message");
        try {
            outToServer.writeBytes(command + "\n");
        } catch (IOException e) {
            System.out.println("TCPConsoleActionMH - write to server " + e);
            e.printStackTrace();
        }

        BufferedReader inFromServer = null;
        try {
            inFromServer = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        } catch (IOException e) {
            System.out.println("TCPConsoleActionMH - creating inputBufferedReader " + e);
            e.printStackTrace();
        }

        String response = null;
        try {
            response = inFromServer.readLine();
        } catch (IOException e) {
            System.out.println("TCPConsoleActionMH - read from server " + e);
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
            System.out.println("TCPConsoleActionMH - closing socket " + e);
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
            System.out.println("TCPConsoleActionMH - creating socket " + e);
            e.printStackTrace();
        }

        DataOutputStream outToServer = null;
        try {
            outToServer = new DataOutputStream(connectionSocket.getOutputStream());
        } catch (IOException e) {
            System.out.println("TCPConsoleActionMH - creating dataOutputStream " + e);
            e.printStackTrace();
        }

        Logger.appDebugLog(command + " output: " + clientNumber);
        try {
            outToServer.writeBytes(command + Config.SENTENCE_SPLITS_CHAR + clientNumber + "\n");
        } catch (IOException e) {
            System.out.println("TCPConsoleActionMH - write to server " + e);
            e.printStackTrace();
        }

        BufferedReader inFromServer = null;
        try {
            inFromServer = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        } catch (IOException e) {
            System.out.println("TCPConsoleActionMH - creating inputBufferedReader " + e);
            e.printStackTrace();
        }

        String response = null;
        try {
            response = inFromServer.readLine();
        } catch (IOException e) {
            System.out.println("TCPConsoleActionMH - read from server " + e);
            e.printStackTrace();
        }
        Logger.appDebugLog(command + " input: " + response);

        try {
            connectionSocket.close();
        } catch (IOException e) {
            System.out.println("TCPConsoleActionMH - closing socket " + e);
            e.printStackTrace();
        }

        Logger.appLog("Connection closed");
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
                return ConsoleCommand.CLOSE.name();
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
}
