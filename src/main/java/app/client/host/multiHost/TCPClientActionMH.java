package app.client.host.multiHost;

import app.client.host.ClientCommand;
import app.config.Config;
import app.utils.ActionUtils;
import app.utils.ConnectionUtils;
import app.utils.FileList;
import app.utils.Logger;
import app.utils.MD5Sum;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class TCPClientActionMH {

    public static void perform(int clientNumber, Socket connectionSocket, String clientSentence) {

        String command = ActionUtils.getCommand(clientSentence);

        switch (ClientCommand.valueOf(command)) {
            case CONNECT:
                connect(clientNumber, connectionSocket, clientSentence);
                break;
            case FILE_LIST:
                getFileList(clientNumber, connectionSocket, clientSentence);
                break;
            case PULL:
                pull(clientNumber, connectionSocket, clientSentence);
                break;
            default:
                Logger.clientLog('"' + command + '"' + " command is not supported yet");
                break;
        }

    }

    private static void connect(int clientNumber, Socket connectionSocket, String clientSentence) {
        Logger.clientDebugLog("fire connect");

        DataOutputStream outToServer = ConnectionUtils.getDataOutputStream(connectionSocket);
        /*DataOutputStream outToServer = null;
        try {
            outToServer = new DataOutputStream(connectionSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        String command = ActionUtils.getCommand(clientSentence);
        String message = ActionUtils.getMessage(clientSentence);
        ConnectionUtils.sendMessageToDataOutputStream(outToServer, command, String.valueOf(clientNumber), message);
        /*try {
            outToServer.writeBytes(command + Config.SPLITS_CHAR + clientNumber +
                    Config.SPLITS_CHAR + message + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Logger.clientDebugLog(command + " output: " + message);*/

        BufferedReader inFromServer = ConnectionUtils.getBufferedReader(connectionSocket);
        /*BufferedReader inFromServer = null;
        try {
            inFromServer = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        String response = null;
        try {
            response = inFromServer.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Logger.clientDebugLog(command + " input: " + response);

        Logger.clientLog("Client " + clientNumber + " has connected to the server");
    }

    private static void getFileList(int clientNumber, Socket connectionSocket, String clientSentence) {
        Logger.clientDebugLog("fire getFileList");

        String command = ActionUtils.getCommand(clientSentence);

        DataOutputStream outToServer = ConnectionUtils.getDataOutputStream(connectionSocket);
        /*DataOutputStream outToServer = null;
        try {
            outToServer = new DataOutputStream(connectionSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        Logger.clientDebugLog(command + " input: " + clientSentence);

        List<String> clientFileList = FileList.packFileInfoList(
                FileList.getFileInfoList(clientNumber)
        );

        String response = String.valueOf(clientFileList.size());
        ConnectionUtils.sendMessageToDataOutputStream(outToServer, command, response);
        /*try {
            outToServer.writeBytes(command + Config.SPLITS_CHAR + response + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Logger.clientDebugLog(command + " input: " + response);*/

        clientFileList.forEach(
                fileData -> {
                    ConnectionUtils.sendMessageToDataOutputStream(outToServer, fileData);
                    /*try {
                        finalOutToServer.writeBytes(fileData + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Logger.clientDebugLog(command + " input: " + fileData);*/
                }
        );

        Logger.clientLog("Client file list sent to server");
    }

    private static void pull(int clientNumber, Socket connectionSocket, String clientSentence) {
        Logger.clientDebugLog("fire pull");

        String command = ActionUtils.getCommand(clientSentence);
        int targetClientNumber = ActionUtils.getClientNumber(clientSentence);
        String fileName = ActionUtils.getFileName(clientSentence);
        String response;

        OutputStream outputStream = null;
        try {
            outputStream = connectionSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        DataOutputStream outToServer = ConnectionUtils.getDataOutputStream(connectionSocket);
        /*DataOutputStream outToServer = null;
        try {
            outToServer = new DataOutputStream(connectionSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        String filePath = Config.BASIC_PATH + clientNumber + "//" + fileName;
        File file = new File(filePath);

        if (file.exists()) {
            response = "Sending file " + fileName + " started";
        } else {
            response = "Client " + clientNumber + " doesn't share file " + fileName +
                    ". Chceck file name and client number";
        }

        ConnectionUtils.sendMessageToDataOutputStream(outToServer, command, String.valueOf(file.exists()), response);
        /*try {
            outToServer.writeBytes(command + Config.SPLITS_CHAR + file.exists() + Config.SPLITS_CHAR + response + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        if (file.exists()) {
            String md5sum = null;
            try {
                md5sum = MD5Sum.md5(Files.readAllBytes(Paths.get(filePath)));
            } catch (IOException e) {
                e.printStackTrace();
            }
            response = "Sending file " + fileName + " md5 sum";
            ConnectionUtils.sendMessageToDataOutputStream(outToServer, command, md5sum, response);
            /*try {
                outToServer.writeBytes(command + Config.SPLITS_CHAR + md5sum + Config.SPLITS_CHAR + response + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }*/

            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            int count;
            byte[] buffer = new byte[Config.BUFFER_SIZE_IN_BYTES];
            try {
                while ((count = fileInputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, count);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Logger.clientLog("Send file " + fileName + " to client " + targetClientNumber);
        }

        ConnectionUtils.closeSocket(connectionSocket);
        /*try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        Logger.clientDebugLog(command + " sending sequence ended");
    }
}
