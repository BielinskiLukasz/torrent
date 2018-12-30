package app.client.console.multiHost;

import app.client.console.ConsoleCommand;
import app.config.Config;
import app.utils.ActionUtils;
import app.utils.ConnectionUtils;
import app.utils.Logger;
import app.utils.MD5Sum;

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
        if (!userSentence.contains(Config.SPLITS_CHAR)) {
            userSentence = addSplitChars(userSentence);
        }
        String command = ActionUtils.getConsoleCommand(userSentence);

        switch (ConsoleCommand.valueOf(command)) {
            case FILE_LIST:
                getFileList(command);
                break;
            case PULL:
                pull(clientNumber, userSentence); // TODO BACKLOG implement checking client connection with server!
                //                                      (trying to download from not connected client throw exception)
                //                                      and protect against file overwriting
                break;
            case PUSH:
                push();
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

    private static String addSplitChars(String userSentence) {
        return userSentence.replaceFirst(" ", Config.SPLITS_CHAR).replaceFirst(" ", Config.SPLITS_CHAR);
    }

    private static void getFileList(String command) {
        Logger.appDebugLog("fire getFileList");

        Socket connectionSocket = ConnectionUtils.createSocket(Config.HOST_IP, Config.PORT_NR);
        /*Socket connectionSocket = null;
        try {
            connectionSocket = new Socket(Config.HOST_IP, Config.PORT_NR);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

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

    private static void pull(int clientNumber, String userSentence) {
        Logger.appDebugLog("fire pull");

        int sourceClientNumber = ActionUtils.getClientNumber(userSentence);

        if (sourceClientNumber == clientNumber) {
            Logger.appLog("It is not the client you are looking for :)");
            Logger.appLog("There is no need to download the file from yourself");
        } else {

            //TODO BACKLOG connect to server and check that selected client is connected with server - here

            Socket connectionSocket = ConnectionUtils.createSocket(Config.HOST_IP,
                    Config.PORT_NR + sourceClientNumber);
            /*Socket connectionSocket = null;
            try {
                connectionSocket = new Socket(Config.HOST_IP, Config.PORT_NR + sourceClientNumber);
            } catch (IOException e) {
                e.printStackTrace();
            }*/

            DataOutputStream outToClient = null;
            try {
                outToClient = new DataOutputStream(connectionSocket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            String command = ActionUtils.getConsoleCommand(userSentence);
            String fileName = ActionUtils.getFileName(userSentence);

            Logger.appDebugLog(command + " output: " + clientNumber + " " + fileName);
            try {
                outToClient.writeBytes(command + Config.SPLITS_CHAR + clientNumber +
                        Config.SPLITS_CHAR + fileName + "\n");
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

            Boolean fileExist = ActionUtils.getBoolean(response);
            String message = ActionUtils.getMessage(response);

            Logger.appLog(message);
            Logger.appDebugLog("" + fileExist);

            if (fileExist) {
                try {
                    response = inFromClient.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Logger.appDebugLog(command + " input: " + response);

                String fileMD5Sum = ActionUtils.getMD5Sum(response);
                String filePath = Config.BASIC_PATH + clientNumber + "//" + fileName;

                File file = new File(filePath);
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

                if (MD5Sum.check(filePath, fileMD5Sum)) {
                    Logger.appLog("File downloaded successfully");
                } else {
                    Logger.appLog("Unsuccessful file download");
                    if (file.delete()) {
                        Logger.appDebugLog("Remove invalid file");
                    }
                }
            }

            ConnectionUtils.closeSocket(connectionSocket);
            /*try {
                connectionSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }*/

            Logger.appLog("Finished");
        }
    }

    private static void push() {

    }

    private static void close(int clientNumber, String command) {
        Logger.appDebugLog("fire close");

        Socket connectionSocket = ConnectionUtils.createSocket(Config.HOST_IP, Config.PORT_NR);
        /*Socket connectionSocket = null;
        try {
            connectionSocket = new Socket(Config.HOST_IP, Config.PORT_NR);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        DataOutputStream outToServer = null;
        try {
            outToServer = new DataOutputStream(connectionSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Logger.appDebugLog(command + " output: " + clientNumber);
        try {
            outToServer.writeBytes(command + Config.SPLITS_CHAR + clientNumber + "\n");
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

        ConnectionUtils.closeSocket(connectionSocket);
        /*try {
            connectionSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        Logger.appLog("Connection closed");
    }
}
