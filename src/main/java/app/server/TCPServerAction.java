package app.server;

import app.client.host.multiHost.ClientCommand;
import app.config.Config;
import app.utils.ActionUtils;
import app.utils.Logger;
import app.utils.SentenceUtils;
import app.utils.TCPConnectionUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

class TCPServerAction {

    static void perform(TCPServer server, Socket connectionSocket, String sentence) {
        Logger.serverDebugLog("perform: " + sentence);

        String command = SentenceUtils.getCommand(sentence);

        switch (ServerCommand.valueOf(command)) {
            case REGISTER:
                register(server, connectionSocket, sentence);
                break;
            case SERVER_FILE_LIST: // TODO handle unconnected client selection by server
                getServerFileList(server, connectionSocket);
                break;
            case CONFIRM_CONNECTION:
                confirmConnection(server, connectionSocket, sentence);
                break;
            case UNREGISTER:
                close(server, connectionSocket, sentence);
                break;
            default:
                sendNotSupportedCommandMessage(connectionSocket, command);
                break;
        }
    }

    private static void register(TCPServer server, Socket connectionSocket, String clientSentence) {
        Logger.serverDebugLog("fire register");

        int clientNumber = getClientDataFromReceiveRequest(clientSentence);
        registerClient(server, clientNumber);
        sendConnectionConfirmation(connectionSocket, clientNumber);

        Logger.serverLog("Connection to client " + clientNumber + " was detected");
    }

    private static int getClientDataFromReceiveRequest(String clientSentence) {
        String command = SentenceUtils.getCommand(clientSentence);
        String message = SentenceUtils.getMessage(clientSentence);
        int clientNumber = SentenceUtils.getClientNumber(clientSentence);
        Logger.serverDebugLog(command + " input: " + message);
        return clientNumber;
    }

    private static void registerClient(TCPServer server, int clientNumber) {
        server.addClient(clientNumber);
    }

    private static void sendConnectionConfirmation(Socket connectionSocket, int clientNumber) {
        DataOutputStream outToClient = TCPConnectionUtils.getDataOutputStream(connectionSocket);
        String response = "Hello client " + clientNumber;
        TCPConnectionUtils.sendMessageToDataOutputStream(outToClient, response);
    }

    private static void confirmConnection(TCPServer server, Socket connectionSocket, String clientSentence) {
        Logger.serverDebugLog("fire confirmConnection");

        int clientNumber = SentenceUtils.getClientNumber(clientSentence);
        boolean sourceClientConnected = server.isClientConnected(clientNumber);

        DataOutputStream outToClient = TCPConnectionUtils.getDataOutputStream(connectionSocket);
        String response = "Client " + clientNumber + " connection confirmation";
        TCPConnectionUtils.sendMessageToDataOutputStream(outToClient, response, String.valueOf(sourceClientConnected));

        Logger.serverLog("Client " + clientNumber + " is connected");
    }

    private static void getServerFileList(TCPServer server, Socket connectionSocket) {
        Logger.serverDebugLog("fire getServerFileList");

        List<String> serverFileList = new ArrayList<>();
        server.getUserList().forEach(
                userNumber -> {
                    Socket userSocket = TCPConnectionUtils.createSocket(Config.HOST_IP, Config.PORT_NR + userNumber);

                    DataOutputStream outToClient = TCPConnectionUtils.getDataOutputStream(userSocket);
                    String command = String.valueOf(ClientCommand.CLIENT_FILE_LIST);
                    TCPConnectionUtils.sendMessageToDataOutputStream(outToClient, command);

                    BufferedReader inFromClient = TCPConnectionUtils.getBufferedReader(userSocket);
                    String response = TCPConnectionUtils.readBufferedReaderLine(inFromClient);

                    int clientFileListSize = SentenceUtils.getListSize(response);
                    for (int i = 0; i < clientFileListSize; i++) {
                        serverFileList.add(
                                TCPConnectionUtils.readBufferedReaderLine(inFromClient)
                        );
                    }

                    TCPConnectionUtils.closeSocket(userSocket);

                    Logger.serverLog("A file list from the client " + userNumber + " was received");
                }
        );

        server.setFileList(serverFileList);

        ActionUtils.sendList(connectionSocket, serverFileList);

        Logger.serverLog("Server file list sent to client ");
    }

    private static void close(TCPServer server, Socket connectionSocket, String clientSentence) {
        Logger.serverDebugLog("fire close");

        int clientNumber = SentenceUtils.getClientNumber(clientSentence);

        server.removeClient(clientNumber);

        String response = "Bye client " + clientNumber;

        DataOutputStream outToClient = TCPConnectionUtils.getDataOutputStream(connectionSocket);
        TCPConnectionUtils.sendMessageToDataOutputStream(outToClient, response);

        Logger.serverLog("Unregister client " + clientNumber);
    }

    private static void sendNotSupportedCommandMessage(Socket connectionSocket, String command) {
        Logger.serverDebugLog("fire sendNotSupportedCommandMessage");

        DataOutputStream outToClient = TCPConnectionUtils.getDataOutputStream(connectionSocket);
        String response = '"' + command + '"' + " command is not supported yet";
        TCPConnectionUtils.sendMessageToDataOutputStream(outToClient, response);

        Logger.serverLog("Not supported command message sent");
    }
}
