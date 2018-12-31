package app.server;

import app.config.Config;
import app.utils.ConnectionUtils;
import app.utils.ConsoleCommandUtils;
import app.utils.Logger;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

class TCPServerAction {

    static void perform(TCPServer server, Socket connectionSocket, String clientSentence) {
        String command = ConsoleCommandUtils.getCommand(clientSentence);

        switch (ServerCommand.valueOf(command)) {
            case CONNECT:
                connect(server, connectionSocket, clientSentence);
                break;
            case FILE_LIST:
                getFileList(server, connectionSocket, command);
                break;
            case CONFIRM_CONNECTION:
                confirmConnection(server, connectionSocket, clientSentence);
                break;
            case CLOSE:
                close(server, connectionSocket, clientSentence);
                break;
            default:
                sendNotSupportedCommandMessage(connectionSocket, command);
                break;
        }
    }

    private static void confirmConnection(TCPServer server, Socket connectionSocket, String clientSentence) {
        Logger.serverDebugLog("fire connect");

        int clientNumber = ConsoleCommandUtils.getClientNumber(clientSentence);
        boolean sourceClientConnected = server.isClientConnected(clientNumber);

        DataOutputStream outToClient = ConnectionUtils.getDataOutputStream(connectionSocket);
        String response = "Client " + clientNumber + " connection confirmation";
        ConnectionUtils.sendMessageToDataOutputStream(outToClient, response, String.valueOf(sourceClientConnected));

        Logger.serverLog("Connection to client " + clientNumber + " was detected");
    }

    private static void connect(TCPServer server, Socket connectionSocket, String clientSentence) {
        Logger.serverDebugLog("fire connect");

        int clientNumber = getClientDataFromReceiveRequest(clientSentence);
        registerClient(server, clientNumber);
        sendConnectionConfirmation(connectionSocket, clientNumber);

        Logger.serverLog("Connection to client " + clientNumber + " was detected");
    }

    private static int getClientDataFromReceiveRequest(String clientSentence) {
        String command = ConsoleCommandUtils.getCommand(clientSentence);
        String message = ConsoleCommandUtils.getMessage(clientSentence);
        int clientNumber = ConsoleCommandUtils.getClientNumber(clientSentence);
        Logger.serverDebugLog(command + " input: " + message);
        return clientNumber;
    }

    private static void registerClient(TCPServer server, int clientNumber) {
        server.addClient(clientNumber);
    }

    private static void sendConnectionConfirmation(Socket connectionSocket, int clientNumber) {
        DataOutputStream outToClient = ConnectionUtils.getDataOutputStream(connectionSocket);
        String response = "Hello client " + clientNumber;
        ConnectionUtils.sendMessageToDataOutputStream(outToClient, response);
    }

    private static void getFileList(TCPServer server, Socket connectionSocket, String command) {
        Logger.serverDebugLog("fire getFileList");
        Logger.serverDebugLog(command + " input: " + "no message");

        List<String> serverFileList = new ArrayList<>();
        server.getUserList().forEach(
                userNumber -> {
                    Socket userSocket = ConnectionUtils.createSocket(Config.HOST_IP, Config.PORT_NR + userNumber);

                    DataOutputStream outToClient = ConnectionUtils.getDataOutputStream(userSocket);
                    ConnectionUtils.sendMessageToDataOutputStream(outToClient, command);

                    BufferedReader inFromClient = ConnectionUtils.getBufferedReader(userSocket);
                    String response = ConnectionUtils.readBufferedReaderLine(inFromClient);

                    int clientFileListSize = ConsoleCommandUtils.getListSize(response);
                    for (int i = 0; i < clientFileListSize; i++) {
                        serverFileList.add(
                                ConnectionUtils.readBufferedReaderLine(inFromClient)
                        );
                    }

                    ConnectionUtils.closeSocket(userSocket);

                    Logger.serverLog("A file list from the client " + userNumber + " was received");
                }
        );

        server.setFileList(serverFileList);

        DataOutputStream outToClient = ConnectionUtils.getDataOutputStream(connectionSocket);
        String response = String.valueOf(serverFileList.size());
        ConnectionUtils.sendMessageToDataOutputStream(outToClient, command, response);

        serverFileList.forEach(
                fileData -> ConnectionUtils.sendMessageToDataOutputStream(outToClient, fileData)
        );

        Logger.serverLog("Server file list sent to client ");
    }

    private static void close(TCPServer server, Socket connectionSocket, String clientSentence) {
        Logger.serverDebugLog("fire close");

        int clientNumber = ConsoleCommandUtils.getClientNumber(clientSentence);

        server.removeClient(clientNumber);

        String response = "Bye client " + clientNumber;

        DataOutputStream outToClient = ConnectionUtils.getDataOutputStream(connectionSocket);
        ConnectionUtils.sendMessageToDataOutputStream(outToClient, response);

        Logger.serverLog("Connection with client " + clientNumber + " was closed");
    }

    private static void sendNotSupportedCommandMessage(Socket connectionSocket, String command) {
        Logger.serverDebugLog("fire sendNotSupportedCommandMessage");

        DataOutputStream outToClient = ConnectionUtils.getDataOutputStream(connectionSocket);
        String response = '"' + command + '"' + " command is not supported yet";
        ConnectionUtils.sendMessageToDataOutputStream(outToClient, response);

        Logger.serverLog("Not supported command message sent");
    }
}
