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
        /*Socket connectionSocket = null;
        try {
            connectionSocket = new Socket(Config.HOST_IP, Config.PORT_NR);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        DataOutputStream outToServer = ConnectionUtils.getDataOutputStream(connectionSocket);
        /*DataOutputStream outToServer = null;
        try {
            outToServer = new DataOutputStream(connectionSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        ConnectionUtils.sendMessageToDataOutputStream(outToServer, command);
        /*try {
            outToServer.writeBytes(command + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Logger.consoleDebugLog(command + " output: " + "no message");*/

        BufferedReader inFromServer = ConnectionUtils.getBufferedReader(connectionSocket);
        /*BufferedReader inFromServer = null;
        try {
            inFromServer = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        String response = ConnectionUtils.readBufferedReaderLine(inFromServer);
        /*String response = null;
        try {
            response = inFromServer.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Logger.consoleDebugLog(command + " input: " + response);*/

        int serverFileListSize = ActionUtils.getListSize(response);
        for (int i = 0; i < serverFileListSize; i++) {
            Logger.consoleLog(
                    ConnectionUtils.readBufferedReaderLine(inFromServer)
                            .replaceAll(String.format("\\%s", Config.FILE_INFO_SPLITS_CHAR), " ")
                    // TODO move getting better format to another place
            );
            /*try {
                Logger.consoleLog(
                        inFromServer.readLine()
                                .replaceAll(String.format("\\%s", Config.FILE_INFO_SPLITS_CHAR), " ")
                );
            } catch (IOException e) {
                e.printStackTrace();
            }*/
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

        int sourceClientNumber = ActionUtils.getClientNumber(userSentence);

        if (sourceClientNumber == clientNumber) {
            Logger.consoleLog("It is not the client you are looking for :)");
            Logger.consoleLog("There is no need to download the file from yourself");
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

            DataOutputStream outToClient = ConnectionUtils.getDataOutputStream(connectionSocket);
            /*DataOutputStream outToClient = null;
            try {
                outToClient = new DataOutputStream(connectionSocket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }*/

            String command = ActionUtils.getConsoleCommand(userSentence);
            String fileName = ActionUtils.getFileName(userSentence);

            ConnectionUtils.sendMessageToDataOutputStream(outToClient, command, String.valueOf(clientNumber), fileName);
            /*try {
                outToClient.writeBytes(command + Config.SPLITS_CHAR + clientNumber +
                        Config.SPLITS_CHAR + fileName + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
            Logger.consoleDebugLog(command + " output: " + clientNumber + " " + fileName);*/

            InputStream inputStream = null;
            try {
                inputStream = connectionSocket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            BufferedReader inFromClient = ConnectionUtils.getBufferedReader(connectionSocket);
            /*BufferedReader inFromClient = null;
            try {
                inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }*/

            String response = ConnectionUtils.readBufferedReaderLine(inFromClient);
            /*String response = null;
            try {
                response = inFromClient.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Logger.consoleDebugLog(command + " input: " + response);*/

            Boolean fileExist = ActionUtils.getBoolean(response);
            String message = ActionUtils.getMessage(response);

            Logger.consoleLog(message);
            Logger.consoleDebugLog("" + fileExist);

            if (fileExist) {
                response = ConnectionUtils.readBufferedReaderLine(inFromClient);
                /*try {
                    response = inFromClient.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Logger.consoleDebugLog(command + " input: " + response);*/

                String fileMD5Sum = ActionUtils.getMD5Sum(response);
                String filePath = Config.BASIC_PATH + clientNumber + "//" + fileName;

                File file = new File(filePath);
                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Logger.consoleDebugLog(command + " open fileOutputStream");

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
                Logger.consoleDebugLog(command + " close fileOutputStream");

                if (MD5Sum.check(filePath, fileMD5Sum)) {
                    Logger.consoleLog("File downloaded successfully");
                } else {
                    Logger.consoleLog("Unsuccessful file download");
                    if (file.delete()) {
                        Logger.consoleDebugLog("Remove invalid file");
                    }
                }
            }

            ConnectionUtils.closeSocket(connectionSocket);
            /*try {
                connectionSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }*/

            Logger.consoleLog("Finished");
        }
    }

    private static void push() {

    }

    private static void close(int clientNumber, String command) {
        Logger.consoleDebugLog("fire close");

        Socket connectionSocket = ConnectionUtils.createSocket(Config.HOST_IP, Config.PORT_NR);
        /*Socket connectionSocket = null;
        try {
            connectionSocket = new Socket(Config.HOST_IP, Config.PORT_NR);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        DataOutputStream outToServer = ConnectionUtils.getDataOutputStream(connectionSocket);
        /*DataOutputStream outToServer = null;
        try {
            outToServer = new DataOutputStream(connectionSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        ConnectionUtils.sendMessageToDataOutputStream(outToServer, command, String.valueOf(clientNumber));
        /*try {
            outToServer.writeBytes(command + Config.SPLITS_CHAR + clientNumber + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Logger.consoleDebugLog(command + " output: " + clientNumber);*/

        BufferedReader inFromServer = ConnectionUtils.getBufferedReader(connectionSocket);
        /*BufferedReader inFromServer = null;
        try {
            inFromServer = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        ConnectionUtils.readBufferedReaderLine(inFromServer);
        /*String response = null;
        try {
            response = inFromServer.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Logger.consoleDebugLog(command + " input: " + response);*/

        ConnectionUtils.closeSocket(connectionSocket);
        /*try {
            connectionSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        Logger.consoleLog("Connection closed");
    }
}
