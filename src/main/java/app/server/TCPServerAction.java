package app.server;

import app.client.host.ClientCommand;
import app.config.Config;
import app.utils.Logger;
import app.utils.connectionUtils.ActionUtils;
import app.utils.connectionUtils.SentenceUtils;
import app.utils.connectionUtils.TCPConnectionUtils;
import app.utils.fileUtils.FileInfo;
import app.utils.fileUtils.FileList;

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
                ActionUtils.sendList(connectionSocket, server.getFileList());
                break;
            case CONFIRM_CONNECTION:
                confirmConnection(server, connectionSocket, sentence);
                break;
            case CLIENTS_WHO_SHARING_FILE:
                getClientsWithFile(server, connectionSocket, sentence);
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
        TCPConnectionUtils.writeMessageToDataOutputStream(outToClient, response);
    }

    private static void confirmConnection(TCPServer server, Socket connectionSocket, String clientSentence) {
        Logger.serverDebugLog("fire confirmConnection");

        int clientNumber = SentenceUtils.getClientNumber(clientSentence);
        boolean sourceClientConnected = server.isClientConnected(clientNumber);

        DataOutputStream outToClient = TCPConnectionUtils.getDataOutputStream(connectionSocket);
        String response = "Client " + clientNumber + " connection confirmation";
        TCPConnectionUtils.writeMessageToDataOutputStream(outToClient, response, String.valueOf(sourceClientConnected));

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
                    TCPConnectionUtils.writeMessageToDataOutputStream(outToClient, command);

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

//        ActionUtils.sendList(connectionSocket, serverFileList); //TODO separate getting and sending file list to reuse getting in multi host part

        Logger.serverLog("Server file list sent to client ");
    }

    private static void getClientsWithFile(TCPServer server, Socket connectionSocket, String sentence) { // TODO create method to handle request with md5sum
        getServerFileList(server, connectionSocket);

        String fileName = SentenceUtils.getFileName(sentence);
        final String[] md5sum = new String[1];
        server.getFileList().forEach( //TODO refactor it !!!
                packedFileData -> {
                    if (FileList.unpackFileInfo(packedFileData).getName().equals(fileName)) {
                        md5sum[0] = FileList.unpackFileInfo(packedFileData).getMd5();
                    }
                }
        );
        List<Integer> usersWithFile = new ArrayList<>();

        server.getFileList().forEach(
                fileData -> {
                    FileInfo fileInfo = FileList.unpackFileInfo(fileData);
                    Logger.serverDebugLog("Look file " + fileName + " in " + fileInfo.getName());
                    if (fileInfo.getName().equals(fileName) && fileInfo.getMd5().equals(md5sum[0])) {
                        usersWithFile.add(fileInfo.getClientId());
                        Logger.serverDebugLog("Found " + fileName + " in " + fileInfo.getName());
//                    } else if (fileInfo.getName().equals(fileName) && !fileInfo.getMd5().equals(md5sum[0])) {
                        // TODO inform about file doubles
                    }
                }
        );

        //TODO implements file doubles (same name, other md5sum) searching / verification
        DataOutputStream outToClient = TCPConnectionUtils.getDataOutputStream(connectionSocket);
        TCPConnectionUtils.writeMessageToDataOutputStream(outToClient, "", String.valueOf(false));

        ActionUtils.sendList(connectionSocket, usersWithFile);
    }

    private static void close(TCPServer server, Socket connectionSocket, String clientSentence) {
        Logger.serverDebugLog("fire close");

        int clientNumber = SentenceUtils.getClientNumber(clientSentence);

        server.removeClient(clientNumber);

        String response = "Bye client " + clientNumber;

        DataOutputStream outToClient = TCPConnectionUtils.getDataOutputStream(connectionSocket);
        TCPConnectionUtils.writeMessageToDataOutputStream(outToClient, response);

        Logger.serverLog("Unregister client " + clientNumber);
    }

    private static void sendNotSupportedCommandMessage(Socket connectionSocket, String command) {
        Logger.serverDebugLog("fire sendNotSupportedCommandMessage");

        DataOutputStream outToClient = TCPConnectionUtils.getDataOutputStream(connectionSocket);
        String response = '"' + command + '"' + " command is not supported yet";
        TCPConnectionUtils.writeMessageToDataOutputStream(outToClient, response);

        Logger.serverLog("Not supported command message sent");
    }
}
