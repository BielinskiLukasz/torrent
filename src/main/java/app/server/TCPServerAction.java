package app.server;

import app.client.host.ClientCommand;
import app.config.Config;
import app.utils.Logger;
import app.utils.connectionUtils.ActionUtils;
import app.utils.connectionUtils.Segment;
import app.utils.connectionUtils.TCPConnectionUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

class TCPServerAction {

    static void perform(TCPServer server, Socket connectionSocket, String packedSegment) {
        Logger.serverDebugLog("perform: " + packedSegment);

        Segment segment = Segment.unpack(packedSegment);

        switch (ServerCommand.valueOf(segment.getCommand())) {
            case REGISTER:
                register(server, connectionSocket, segment);
                break;
            case SERVER_FILE_LIST: // TODO BACKLOG handle unconnected client selection by server
                getServerFileList(server); // TODO BACKLOG getting file list for all users in the same time (threads);
                ActionUtils.sendList(connectionSocket, server.getFileList(), segment);
                break;
            case CONFIRM_CONNECTION:
                confirmConnection(server, connectionSocket, segment);
                break;
            /*case CLIENTS_WHO_SHARING_FILE:
                getClientsWithFile(server, connectionSocket, segment);
                break;
            case CLIENTS_WHO_SHARING_SPECIFIC_FILE:
                getClientsWithSpecificFile(server, connectionSocket, packedSegment);
                break;
            case UNREGISTER:
                close(server, connectionSocket, packedSegment);
                break;
            default:
                sendNotSupportedCommandMessage(connectionSocket, segment.getCommand());
                break;*/ // TODO turn on this
        }
    }

    private static void register(TCPServer server, Socket connectionSocket, Segment registerSegment) {
        Logger.serverDebugLog("fire register " + registerSegment);

        int clientNumber = registerSegment.getSourceClient();
        registerClient(server, clientNumber);
        sendConnectionConfirmation(connectionSocket, clientNumber);

        Logger.serverLog("Connection to client " + clientNumber + " was detected");
    }

    private static void registerClient(TCPServer server, int clientNumber) {
        server.addClient(clientNumber);
    }

    private static void sendConnectionConfirmation(Socket connectionSocket, int clientNumber) {
        DataOutputStream outToClient = TCPConnectionUtils.getDataOutputStream(connectionSocket);
        Segment connectionConfirmationSegment = Segment.getBuilder()
                .setSourceClient(0)
                .setDestinationClient(clientNumber)
                .setMessage("Hello client " + clientNumber)
                .setComment("send client connection with server confirmation")
                .build();
        TCPConnectionUtils.writeSegmentToDataOutputStream(outToClient, connectionConfirmationSegment);
    }

    private static void confirmConnection(TCPServer server, Socket connectionSocket, Segment pingSegment) {
        Logger.serverDebugLog("fire confirmConnection");

        int targetClientNumber = pingSegment.getDestinationClient();
        boolean sourceClientConnected = server.isClientConnected(targetClientNumber);

        DataOutputStream outToClient = TCPConnectionUtils.getDataOutputStream(connectionSocket);
        Segment pingResponse = Segment.getBuilder()
                .setSourceClient(targetClientNumber)
                .setDestinationClient(pingSegment.getSourceClient())
                .setFlag(sourceClientConnected)
                .setMessage("Ping response")
                .setComment("send information about client " + targetClientNumber + " connection with server")
                .build();
        TCPConnectionUtils.writeSegmentToDataOutputStream(outToClient, pingResponse);

        Logger.serverLog("Client " + targetClientNumber + " is connected: " + sourceClientConnected);
    }

    private static void getServerFileList(TCPServer server) { // TODO BACKLOG secure after unconnected client (stopped)
        Logger.serverDebugLog("fire getServerFileList");

        List<String> serverFileList = new ArrayList<>();
        server.getUserList().forEach(
                userNumber -> {
                    Socket userSocket = TCPConnectionUtils.createSocket(Config.HOST_IP,
                            Config.PORT_NR + userNumber);

                    DataOutputStream outToClient = TCPConnectionUtils.getDataOutputStream(userSocket);
                    Segment getListSegment = Segment.getBuilder()
                            .setSourceClient(0)
                            .setDestinationClient(userNumber)
                            .setCommand(ClientCommand.CLIENT_FILE_LIST.name())
                            .setComment("send client list request")
                            .build();
                    TCPConnectionUtils.writeSegmentToDataOutputStream(outToClient, getListSegment);

                    BufferedReader inFromClient = TCPConnectionUtils.getBufferedReader(userSocket);
                    Segment listSizeSegment = Segment.unpack(TCPConnectionUtils.readBufferedReaderLine(inFromClient));

                    int clientFileListSize = listSizeSegment.getListSize();
                    for (int i = 0; i < clientFileListSize; i++) {
                        serverFileList.add(
                                Segment.unpack(TCPConnectionUtils.readBufferedReaderLine(inFromClient)).getMessage()
                        );
                    }

                    TCPConnectionUtils.closeSocket(userSocket);

                    Logger.serverDebugLog("A file list from the client " + userNumber + " was received");
                }
        );

        server.setFileList(serverFileList);

        Logger.serverLog("Server file list sent to client ");
    }

    /*private static void getClientsWithFile(TCPServer server, Socket connectionSocket, String sentence) {
        getServerFileList(server);

        String fileName = SentenceUtils.getFileName(sentence);
        final String[] md5sum = new String[1];
        server.getFileList().forEach(
                packedFileData -> {
                    if (FileList.unpackFileInfo(packedFileData).getName().equals(fileName)) {
                        md5sum[0] = FileList.unpackFileInfo(packedFileData).getMd5();
                    }
                }
        );
        List<Integer> usersWithFile = new ArrayList<>();

        boolean differentFileWithSameName = false;
        for (String fileData : server.getFileList()) {
            FileInfo fileInfo = FileList.unpackFileInfo(fileData);
            Logger.serverDebugLog("Look file " + fileName + " in " + fileInfo.getName());
            if (fileInfo.getName().equals(fileName) && fileInfo.getMd5().equals(md5sum[0])) {
                usersWithFile.add(fileInfo.getClientId());
                Logger.serverDebugLog("Found " + fileName + " in " + fileInfo.getName());
            } else if (fileInfo.getName().equals(fileName) && !fileInfo.getMd5().equals(md5sum[0])) {
                Logger.serverDebugLog("Found doubles");
                differentFileWithSameName = true;
            }
        }

        DataOutputStream outToClient = TCPConnectionUtils.getDataOutputStream(connectionSocket);
        TCPConnectionUtils.writeSegmentToDataOutputStream(outToClient,
                "",
                String.valueOf(differentFileWithSameName));

        ActionUtils.sendList(connectionSocket, usersWithFile);
    }

    private static void getClientsWithSpecificFile(TCPServer server, Socket connectionSocket, String sentence) {
        getServerFileList(server);

        String fileName = SentenceUtils.getFileName(sentence);
        String md5sum = SentenceUtils.getMD5Sum(sentence);
        List<Integer> usersWithFile = new ArrayList<>();

        boolean differentFileWithSameName = false;
        for (String fileData : server.getFileList()) {
            FileInfo fileInfo = FileList.unpackFileInfo(fileData);
            Logger.serverDebugLog("Look file " + fileName + " in " + fileInfo.getName());
            if (fileInfo.getName().equals(fileName) && fileInfo.getMd5().equals(md5sum)) {
                usersWithFile.add(fileInfo.getClientId());
                Logger.serverDebugLog("Found " + fileName + " in " + fileInfo.getName());
            } else if (fileInfo.getName().equals(fileName) && !fileInfo.getMd5().equals(md5sum)) {
                Logger.serverDebugLog("Found doubles");
                differentFileWithSameName = true;
            }
        }

        DataOutputStream outToClient = TCPConnectionUtils.getDataOutputStream(connectionSocket);
        TCPConnectionUtils.writeSegmentToDataOutputStream(outToClient,
                "",
                String.valueOf(differentFileWithSameName));

        ActionUtils.sendList(connectionSocket, usersWithFile);
    }

    private static void close(TCPServer server, Socket connectionSocket, String clientSentence) {
        Logger.serverDebugLog("fire close");

        int clientNumber = SentenceUtils.getClientNumber(clientSentence);

        server.removeClient(clientNumber);

        String response = "Bye client " + clientNumber;

        DataOutputStream outToClient = TCPConnectionUtils.getDataOutputStream(connectionSocket);
        TCPConnectionUtils.writeSegmentToDataOutputStream(outToClient, response);

        Logger.serverLog("Unregister client " + clientNumber);
    }

    private static void sendNotSupportedCommandMessage(Socket connectionSocket, String command) {
        Logger.serverDebugLog("fire sendNotSupportedCommandMessage");

        DataOutputStream outToClient = TCPConnectionUtils.getDataOutputStream(connectionSocket);
        String response = '"' + command + '"' + " command is not supported yet";
        TCPConnectionUtils.writeSegmentToDataOutputStream(outToClient, response);

        Logger.serverLog("Not supported command message sent");
    }*/ // TODO turn on this
}
