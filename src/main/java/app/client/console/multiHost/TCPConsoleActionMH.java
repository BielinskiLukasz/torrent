package app.client.console.multiHost;

import app.client.console.ConsoleCommand;
import app.client.host.ClientCommand;
import app.config.Config;
import app.server.ServerCommand;
import app.utils.Logger;
import app.utils.connectionUtils.ActionUtils;
import app.utils.connectionUtils.CommandUtils;
import app.utils.connectionUtils.SentenceUtils;
import app.utils.connectionUtils.TCPConnectionUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

class TCPConsoleActionMH {

    public static void perform(int clientNumber, String userSentence) {
        Logger.consoleDebugLog("perform: " + userSentence);

        userSentence = SentenceUtils.cleanUserSentence(userSentence);
        String command = CommandUtils.getConsoleCommand(userSentence);
        userSentence = SentenceUtils.setClientNumber(userSentence, 0);

        Logger.consoleDebugLog("perform after clean: " + userSentence);

        switch (ConsoleCommand.valueOf(command)) {
            case FILE_LIST:
                getFileList();
                break;
            case PULL: // TODO BACKLOG print message in console if host haven't file
                pull(clientNumber, userSentence);
                break;
            case PUSH:
                push(clientNumber, userSentence);
                break;
            case MULTIPLE_PULL:
                multiplePull(clientNumber, userSentence);
                break;
            case CLOSE:
                close(clientNumber);
                break;
            case EMPTY_COMMAND:
                break;
            case UNSUPPORTED_COMMAND:
            default:
                Logger.consoleLog("command is not supported");
                break;

            // TODO create function classes for each feature (CONNECT, FILE_LIST, PULL (MULTI_PULL), PUSH, CLOSE)
            //  with have method connected with action e.g. ClientA sending message - PUSH (client number),
            //  ClientB receiving - PULL (connectionSocket)

            // TODO Handling restart pull and push there
            //  Refactor restart push - use existing (could be closed) connection for get info about sent file,
            //  and if it's finished successfully then do nothing, else repush - if client who check file was pull
            //  initiator then he should fire pull action
            //  e.g. method
            //  PullUtils
            //  pull
            //  pullToRequest
            //  rePull
            //  rePullToRequest ??

            // TODO create protocol message creators (Builder?), create also protocol reading methods
        }
    }

    private static void getFileList() {
        Logger.consoleDebugLog("fire getFileList");

        Socket connectionSocket = TCPConnectionUtils.createSocket(Config.HOST_IP, Config.PORT_NR);

        DataOutputStream outToServer = TCPConnectionUtils.getDataOutputStream(connectionSocket);
        String command = String.valueOf(ServerCommand.SERVER_FILE_LIST);
        TCPConnectionUtils.writeMessageToDataOutputStream(outToServer, command);

        BufferedReader inFromServer = TCPConnectionUtils.getBufferedReader(connectionSocket);
        String response = TCPConnectionUtils.readBufferedReaderLine(inFromServer);

        int serverFileListSize = SentenceUtils.getListSize(response);
        for (int i = 0; i < serverFileListSize; i++) {
            Logger.consoleLog(
                    TCPConnectionUtils.readBufferedReaderLine(inFromServer)
                            .replaceAll(String.format("\\%s", Config.FILE_INFO_SPLITS_CHAR), " ")
                    // TODO move getting better format to another place
            );
        }

        TCPConnectionUtils.closeSocket(connectionSocket);

        Logger.consoleLog("Server file list was displayed");
    }

    private static void pull(int clientNumber, String userSentence) {
        Logger.consoleDebugLog("fire pull");

        int sourceClientNumber = SentenceUtils.getClientNumber(userSentence);

        if (isClientChooseHisOwnNumber(clientNumber, sourceClientNumber)) {
            Logger.consoleLog("This is not the client you are looking for :)");
            Logger.consoleLog("There is no need to download the file from yourself");
        } else {
            if (ActionUtils.isSelectedClientConnected(sourceClientNumber)) {

                Socket hostConnectionSocket = TCPConnectionUtils.createSocket(Config.HOST_IP,
                        Config.PORT_NR + sourceClientNumber);

                DataOutputStream outToClient = TCPConnectionUtils.getDataOutputStream(hostConnectionSocket);
                String command = String.valueOf(ClientCommand.PUSH_ON_DEMAND);
                String fileName = SentenceUtils.getFileName(userSentence);
                TCPConnectionUtils.writeMessageToDataOutputStream(outToClient, command, String.valueOf(clientNumber), fileName);

                TCPConnectionUtils.closeSocket(hostConnectionSocket);

                Logger.consoleLog("Sending push request");
                Logger.consoleLog("Finished");
            } else {
                Logger.consoleLog("Client " + sourceClientNumber + " isn't connected");
            }
        }
    }

    private static void push(int clientNumber, String userSentence) {
        Logger.consoleDebugLog("fire push");

        int targetClientNumber = SentenceUtils.getClientNumber(userSentence);
        String fileName = SentenceUtils.getFileName(userSentence);

        if (isClientChooseHisOwnNumber(clientNumber, targetClientNumber)) {
            Logger.consoleLog("This is not the client you are looking for :)");
            Logger.consoleLog("There is no need to upload the file to yourself");
        } else if (ActionUtils.isSelectedClientConnected(targetClientNumber)) {
            ActionUtils.uploadIfFileExist(clientNumber, targetClientNumber, fileName);
        } else {
            Logger.consoleLog("You haven't selected file");
        }
    }

    private static boolean isClientChooseHisOwnNumber(int clientNumber, int targetClientNumber) {
        return targetClientNumber == clientNumber;
    }

    private static void multiplePull(int clientNumber, String userSentence) {
        Logger.consoleDebugLog("fire close");

        Socket connectionSocket = TCPConnectionUtils.createSocket(Config.HOST_IP, Config.PORT_NR);

        DataOutputStream outToServer = TCPConnectionUtils.getDataOutputStream(connectionSocket);
        String command = String.valueOf(ServerCommand.CLIENTS_WHO_SHARING_FILE);
        String fileName = SentenceUtils.getFileName(userSentence);
        if (SentenceUtils.getSentenceSize(userSentence) > 3) {
            String fileMD5Sum = SentenceUtils.getMD5Sum(userSentence);
            TCPConnectionUtils.writeMessageToDataOutputStream(outToServer,
                    command,
                    String.valueOf(clientNumber),
                    fileName,
                    fileMD5Sum);
        } else {
            TCPConnectionUtils.writeMessageToDataOutputStream(outToServer,
                    command,
                    String.valueOf(clientNumber),
                    fileName);
        }

        BufferedReader inFromServer = TCPConnectionUtils.getBufferedReader(connectionSocket);
        String response = TCPConnectionUtils.readBufferedReaderLine(inFromServer); // info about file doubles

        Boolean isAnyDifferentFileWithTheSameName = SentenceUtils.getBoolean(response);
        if (isAnyDifferentFileWithTheSameName) {
            Logger.consoleLog("There are different files with the same file name. You must specify md5sum in request");
            TCPConnectionUtils.closeSocket(connectionSocket);
        } else {
            response = TCPConnectionUtils.readBufferedReaderLine(inFromServer); // info about list size

            int serverFileListSize = SentenceUtils.getListSize(response);
            Logger.consoleLog(serverFileListSize + " users have file " + fileName);

            Set<Integer> usersWithFile = new HashSet<>();
            for (int i = 0; i < serverFileListSize; i++) {
                String user = TCPConnectionUtils.readBufferedReaderLine(inFromServer);
                usersWithFile.add(Integer.parseInt(user));
                Logger.consoleLog(user);
            }

            TCPConnectionUtils.closeSocket(connectionSocket);

            //get file size
            int clientWithFile = (!usersWithFile.stream().findFirst().isPresent() ? 0 :
                    usersWithFile.stream().findFirst().get());
            //TODO ignore when clientWithFile = 0
            connectionSocket = TCPConnectionUtils.createSocket(Config.HOST_IP, Config.PORT_NR + clientWithFile);

            DataOutputStream outToClient = TCPConnectionUtils.getDataOutputStream(connectionSocket);
            command = String.valueOf(ClientCommand.CLIENT_FILE_INFO);
            TCPConnectionUtils.writeMessageToDataOutputStream(outToClient,
                    command,
                    String.valueOf(clientWithFile),
                    fileName);

            BufferedReader inFromClient = TCPConnectionUtils.getBufferedReader(connectionSocket);
            response = TCPConnectionUtils.readBufferedReaderLine(inFromClient);
            Long fileSize = Long.parseLong(SentenceUtils.getFileSize(response));

            TCPConnectionUtils.closeSocket(connectionSocket);

            int position = 0;
            int usersWithFileNumber = usersWithFile.size();
            long stepSize = fileSize / usersWithFileNumber + 1;
            for (int userWithFile : usersWithFile) { //TODO tests, implements threads in future
                long startByteNum = stepSize * position++;
                long endByteNum = stepSize * position - 1;
                int packetNumber = position;
                // TODO ignore packet where endByte <= startByte (remove half of users/download all file from one user
                //  when file size is less than config.min)

                connectionSocket = TCPConnectionUtils.createSocket(Config.HOST_IP, Config.PORT_NR + userWithFile);

                outToClient = TCPConnectionUtils.getDataOutputStream(connectionSocket);
                command = String.valueOf(ClientCommand.HANDLE_PUSH_PACK);
                TCPConnectionUtils.writeMessageToDataOutputStream(outToClient,
                        command,
                        String.valueOf(clientNumber),
                        fileName,
                        String.valueOf(startByteNum),
                        String.valueOf(endByteNum),
                        String.valueOf(packetNumber)); // TODO better send part size and packet number

                /*inFromClient = TCPConnectionUtils.getBufferedReader(connectionSocket);
                String sentence = TCPConnectionUtils.readBufferedReaderLine(inFromClient);
                String fileMD5Sum = SentenceUtils.getMD5Sum(sentence);

                String filePath = Config.BASIC_PATH + clientNumber + "//" + fileName;
                File file = new File(filePath);
                FileOutputStream fileOutputStream = TCPConnectionUtils.createFileOutputStream(file);
                InputStream inputStream = TCPConnectionUtils.getInputStream(connectionSocket);
                TCPConnectionUtils.readFileFromStream(fileOutputStream, inputStream);
                TCPConnectionUtils.closeFileOutputStream(fileOutputStream);*/


                TCPConnectionUtils.closeSocket(connectionSocket);

                Logger.consoleDebugLog(startByteNum + " " + endByteNum);
            }


        }

        TCPConnectionUtils.closeSocket(connectionSocket);
    }

    private static void close(int clientNumber) {
        Logger.consoleDebugLog("fire close");

        Socket connectionSocket = TCPConnectionUtils.createSocket(Config.HOST_IP, Config.PORT_NR);

        DataOutputStream outToServer = TCPConnectionUtils.getDataOutputStream(connectionSocket);
        String command = String.valueOf(ServerCommand.UNREGISTER);
        TCPConnectionUtils.writeMessageToDataOutputStream(outToServer, command, String.valueOf(clientNumber));

        BufferedReader inFromServer = TCPConnectionUtils.getBufferedReader(connectionSocket);
        TCPConnectionUtils.readBufferedReaderLine(inFromServer);

        TCPConnectionUtils.closeSocket(connectionSocket);

        Logger.consoleLog("Connection closed");
    }
}
