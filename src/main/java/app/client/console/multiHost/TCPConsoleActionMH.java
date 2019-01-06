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
import app.utils.fileUtils.MD5Sum;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

class TCPConsoleActionMH {

    public static void perform(int clientNumber, String userSentence) {
        Logger.consoleDebugLog("perform: " + userSentence);

        userSentence = SentenceUtils.cleanUserSentence(userSentence);
        Logger.consoleDebugLog("perform after clean: " + userSentence);
        String command = CommandUtils.getConsoleCommand(userSentence);
        userSentence = SentenceUtils.setClientNumber(userSentence, 0);

        Logger.consoleDebugLog("perform after set number: " + userSentence);

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
                if (SentenceUtils.getSentenceSize(userSentence) == 4) {
                    multiplePull(clientNumber, userSentence, true);
                } else {
                    multiplePull(clientNumber, userSentence, false);
                }
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

            // TODO BACKLOG create function classes for each feature (CONNECT, FILE_LIST, PULL (MULTI_PULL), PUSH, CLOSE)
            //  with have method connected with action e.g. ClientA sending message - PUSH (client number),
            //  ClientB receiving - PULL (connectionSocket)

            // TODO BACKLOG Handling restart pull and push there
            //  Refactor restart push - use existing (could be closed) connection for get info about sent file,
            //  and if it's finished successfully then do nothing, else rePush - if client who check file was pull
            //  initiator then he should fire pull action
            //  e.g. method
            //  PullUtils
            //  pull
            //  pullToRequest
            //  rePull
            //  rePullToRequest ??

            // TODO BACKLOG create protocol message creators (Builder?), create also protocol reading methods
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
                    // TODO BACKLOG move getting better format to another place
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

    private static void multiplePull(int clientNumber, String userSentence, boolean md5SumDefined) {
        Logger.consoleDebugLog("fire close");

        Socket connectionSocket = TCPConnectionUtils.createSocket(Config.HOST_IP, Config.PORT_NR);

        DataOutputStream outToServer = TCPConnectionUtils.getDataOutputStream(connectionSocket);
        String command;
        if (md5SumDefined) {
            command = String.valueOf(ServerCommand.CLIENTS_WHO_SHARING_SPECIFIC_FILE);
        } else {
            command = String.valueOf(ServerCommand.CLIENTS_WHO_SHARING_FILE);
        }
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

        Boolean differentFileWithSameName = SentenceUtils.getBoolean(response);
        if (differentFileWithSameName) {
            Logger.consoleLog("There are different files with the same file name. You must specify md5sum in request. " +
                    "Use MULTIPLE_PUSH_SPECIFIC command");
            TCPConnectionUtils.closeSocket(connectionSocket);
        } else {
            response = TCPConnectionUtils.readBufferedReaderLine(inFromServer);

            int serverFileListSize = SentenceUtils.getListSize(response);
            Logger.consoleLog(serverFileListSize + " users have file " + fileName);

            List<Integer> usersWithFile = new ArrayList<>();
            for (int i = 0; i < serverFileListSize; i++) {
                String user = TCPConnectionUtils.readBufferedReaderLine(inFromServer);
                usersWithFile.add(Integer.parseInt(user));
                Logger.consoleLog(user);
            }

            TCPConnectionUtils.closeSocket(connectionSocket);

            int clientWithFile = (!usersWithFile.stream().findFirst().isPresent() ? 0 :
                    usersWithFile.stream().findFirst().get());
            if (clientWithFile == 0) {
                Logger.consoleLog("No one share a " + fileName + " file");
            } else {
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
                String fileMD5Sum = SentenceUtils.getMD5Sum(response);

                TCPConnectionUtils.closeSocket(connectionSocket);

                int position = 0;
                int usersWithFileNumber = usersWithFile.size();
                long stepSize = fileSize / usersWithFileNumber + 1;
                Thread[] multipleSenders = new Thread[usersWithFileNumber];

                for (int i = 0; i < usersWithFile.size(); i++) {
                    int userWithFile = usersWithFile.get(i);
                    multipleSenders[i] = new MultipleDownloadManager(clientNumber,
                            fileName,
                            userWithFile,
                            position++,
                            stepSize,
                            usersWithFile
                    );
                    multipleSenders[i].start();
                }

                for (int i = 0; i < usersWithFileNumber; i++) {
                    try {
                        multipleSenders[i].join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                String filePath = Config.BASIC_PATH + clientNumber + "//" + fileName;

                try (FileOutputStream stream = new FileOutputStream(filePath, true)) {
                    for (int i = 0; i < usersWithFileNumber; i++) {
                        File partFile = new File(filePath + ".part_" + i);
                        byte[] fileContent = Files.readAllBytes(partFile.toPath());
                        stream.write(fileContent);
                        if (partFile.delete())
                            Logger.clientDebugLog("Remove part " + i);
                        Logger.consoleDebugLog("Combine part " + i);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (MD5Sum.check(filePath, fileMD5Sum)) {
                    Logger.clientDebugLog("File downloaded successfully");
                } else {
                    Logger.clientDebugLog("Unsuccessful file download");
                    multiplePull(clientNumber, userSentence, md5SumDefined);
                }
            }
        }

        TCPConnectionUtils.closeSocket(connectionSocket);
    }

    // TODO inform when user try download file which is actually in his dir

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
