package app.client.console.multiHost;

import app.client.host.ClientCommand;
import app.config.Config;
import app.server.ServerCommand;
import app.utils.ExceptionHandler;
import app.utils.Logger;
import app.utils.connectionUtils.ActionUtils;
import app.utils.connectionUtils.Segment;
import app.utils.connectionUtils.TCPConnectionUtils;
import app.utils.connectionUtils.UserSentence;
import app.utils.fileUtils.MD5Sum;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import static java.lang.Thread.sleep;

class TCPConsoleActionMH {

    public static void perform(int clientNumber, String sentenceFromConsole) {
        Logger.consoleDebugLog("perform: " + sentenceFromConsole);

        UserSentence userSentence = UserSentence.getUserSentence(sentenceFromConsole);

        switch (userSentence.getUserCommand()) {
            case FILE_LIST:
                getFileList(clientNumber);
                break;
            case PULL: // TODO BACKLOG print message in console if host haven't file
                pull(clientNumber, userSentence);
                break;
           /* case PUSH:
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
                break;*/ // TODO turn on this

            // TODO BACKLOG rename TCP to Tcp

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

            // TODO BACKLOG to handle exception without cpu dead
            //      synchronized (this) {
            //                    this.wait();
            //                }
            //            }catch (InterruptedException e){
            //            }
            //        }
        }
    }

    private static void getFileList(int clientNumber) {
        Logger.consoleDebugLog("fire getFileList ");

        Socket connectionSocket = TCPConnectionUtils.createSocket(Config.HOST_IP, Config.PORT_NR);
        DataOutputStream outToServer = TCPConnectionUtils.getDataOutputStream(connectionSocket);
        Segment getFileListSegment = Segment.getBuilder()
                .setSourceClient(clientNumber)
                .setDestinationClient(0)
                .setCommand(ServerCommand.SERVER_FILE_LIST.name())
                .setComment("send list request to server")
                .build();
        TCPConnectionUtils.writeSegmentToDataOutputStream(outToServer, getFileListSegment);

        BufferedReader inFromServer = TCPConnectionUtils.getBufferedReader(connectionSocket);
        Segment listSizeSegment = Segment.unpack(TCPConnectionUtils.readBufferedReaderLine(inFromServer));

        int serverFileListSize = listSizeSegment.getListSize();
        for (int i = 0; i < serverFileListSize; i++) {
            Logger.consoleLog(
                    Segment.unpack(TCPConnectionUtils.readBufferedReaderLine(inFromServer))
                            .getMessage()
                            .replaceAll(String.format("\\%s", Config.FILE_INFO_SPLITS_CHAR), " ")
                    // TODO BACKLOG move getting better format to another place
            );
        }

        TCPConnectionUtils.closeSocket(connectionSocket);

        Logger.consoleLog("End files list");
    }

    private static void pull(int clientNumber, UserSentence userSentence) {
        Logger.consoleDebugLog("fire pull");

        int targetClientNumber = userSentence.getTargetClient();

        if (isClientChooseHisOwnNumber(clientNumber, targetClientNumber)) {
            Logger.consoleLog("This is not the client you are looking for :)");
            Logger.consoleLog("There is no need to download the file from yourself");
        } else {
            if (ActionUtils.isSelectedClientConnected(clientNumber, targetClientNumber)) {

                String fileName = userSentence.getFileName();

                Socket connectionSocket = TCPConnectionUtils.createSocket(
                        Config.HOST_IP, Config.PORT_NR + targetClientNumber);

                DataOutputStream outToClient = TCPConnectionUtils.getDataOutputStream(connectionSocket);
                Segment existRequest = Segment.getBuilder()
                        .setSourceClient(clientNumber)
                        .setDestinationClient(targetClientNumber)
                        .setCommand(ClientCommand.HANDLE_PULL.name())
                        .setFileName(fileName)
                        .setComment("send file exist request")
                        .build();
                TCPConnectionUtils.writeSegmentToDataOutputStream(outToClient, existRequest);

                BufferedReader inFromServer = TCPConnectionUtils.getBufferedReader(connectionSocket);
                Segment fileDetails = Segment.unpack(TCPConnectionUtils.readBufferedReaderLine(inFromServer));

                if (fileDetails.getFlag()) {
                    String filePath = Config.BASIC_PATH + clientNumber + "//" + fileName;
                    File file = new File(filePath);

                    Segment pullRequest = Segment.getBuilder()
                            .setSourceClient(clientNumber)
                            .setDestinationClient(targetClientNumber)
                            .setFileName(fileName)
                            .setStartByteNumber(file.length())
                            .setEndByteNumber(fileDetails.getFileSize())
                            .setComment("send file pull request")
                            .build();
                    TCPConnectionUtils.writeSegmentToDataOutputStream(outToClient, pullRequest);

                    FileOutputStream fileOutputStream = TCPConnectionUtils.createFileOutputStream(file);
                    InputStream inputStream = TCPConnectionUtils.getInputStream(connectionSocket);
                    TCPConnectionUtils.readFileFromStream(fileOutputStream, inputStream);
                    TCPConnectionUtils.closeFileOutputStream(fileOutputStream);

                    Logger.consoleDebugLog("Receiving file " + fileName + " from client " + targetClientNumber + " ends");

                    if (MD5Sum.check(filePath, fileDetails.getMd5Sum())) {
                        Logger.clientLog("File downloaded successfully");
                    } else {
                        if (file.length() >= fileDetails.getFileSize()) {
                            Logger.clientLog("Remove invalid file");
                            file.delete();
                        }
                        Logger.clientLog("Unsuccessful file download");
//                        repull(clientNumber, userSentence); //TODO implement
                    }
                } else {
                    Logger.clientLog("Client " + targetClientNumber + " haven't " + fileName + " file");
                }

                TCPConnectionUtils.closeSocket(connectionSocket);

                Logger.consoleLog("Sending push request"); // TODO refactor message
            } else {
                Logger.consoleLog("Client " + targetClientNumber + " isn't connected");
            }
        }
    }

    private static void repull(int clientNumber, UserSentence userSentence) { // TODO implements repull method
        Logger.consoleDebugLog("fire pull");

        int targetClientNumber = userSentence.getTargetClient();

        if (isClientChooseHisOwnNumber(clientNumber, targetClientNumber)) {
            Logger.consoleLog("This is not the client you are looking for :)");
            Logger.consoleLog("There is no need to download the file from yourself");
        } else {
            if (ActionUtils.isSelectedClientConnected(clientNumber, targetClientNumber)) {

                String fileName = userSentence.getFileName();

                Socket connectionSocket = null;
                boolean reconnect = false;

                for (int i = 0; i < Config.WAITING_TIME_SEC; i++) {
                    try {
                        Logger.clientDebugLog("Try connect with client " + targetClientNumber);
                        connectionSocket = new Socket(Config.HOST_IP, Config.PORT_NR + targetClientNumber);
                        reconnect = true;
                        break;
                    } catch (IOException e) {
                        ExceptionHandler.handle(e);
                    }

                    try { // TODO sleep
                        sleep(1000);
                    } catch (InterruptedException e) {
                        ExceptionHandler.handle(e);
                    }
                }

                if (reconnect) {
                    DataOutputStream outToClient = TCPConnectionUtils.getDataOutputStream(connectionSocket);
                    Segment existRequest = Segment.getBuilder()
                            .setSourceClient(clientNumber)
                            .setDestinationClient(targetClientNumber)
                            .setCommand(ClientCommand.HANDLE_PULL.name())
                            .setFileName(fileName)
                            .setComment("send file exist request")
                            .build();
                    TCPConnectionUtils.writeSegmentToDataOutputStream(outToClient, existRequest);

                    BufferedReader inFromServer = TCPConnectionUtils.getBufferedReader(connectionSocket);
                    Segment fileDetails = Segment.unpack(TCPConnectionUtils.readBufferedReaderLine(inFromServer));

                    if (fileDetails.getFlag()) {
                        String filePath = Config.BASIC_PATH + clientNumber + "//" + fileName;
                        File file = new File(filePath);

                        Segment pullRequest = Segment.getBuilder()
                                .setSourceClient(clientNumber)
                                .setDestinationClient(targetClientNumber)
                                .setFileName(fileName)
                                .setStartByteNumber(file.length())
                                .setEndByteNumber(fileDetails.getFileSize())
                                .setComment("send file pull request")
                                .build();
                        TCPConnectionUtils.writeSegmentToDataOutputStream(outToClient, pullRequest);

                        FileOutputStream fileOutputStream = TCPConnectionUtils.createFileOutputStream(file);
                        InputStream inputStream = TCPConnectionUtils.getInputStream(connectionSocket);
                        TCPConnectionUtils.readFileFromStream(fileOutputStream, inputStream);
                        TCPConnectionUtils.closeFileOutputStream(fileOutputStream);

                        Logger.consoleDebugLog("Receiving file " + fileName + " from client " + targetClientNumber + " ends");

                        if (MD5Sum.check(filePath, fileDetails.getMd5Sum())) {
                            Logger.clientLog("File downloaded successfully");
                        } else {
                            if (file.length() >= fileDetails.getFileSize()) {
                                Logger.clientLog("Remove invalid file");
                                file.delete();
                            }
                            Logger.clientLog("Unsuccessful file download");
                            pull(clientNumber, userSentence);
                        }
                    } else {
                        Logger.clientLog("Client " + targetClientNumber + " haven't " + fileName + " file");
                    }

                    TCPConnectionUtils.closeSocket(connectionSocket);

                    Logger.consoleLog("Sending push request"); // TODO refactor message
                } else {
                    Logger.consoleLog("Cannot connect with client " + targetClientNumber);
                }
            } else {
                Logger.consoleLog("Client " + targetClientNumber + " isn't connected");
            }
        }
    }

    /*private static void push(int clientNumber, String userSentence) {
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
    }*/

    private static boolean isClientChooseHisOwnNumber(int clientNumber, int targetClientNumber) {
        return targetClientNumber == clientNumber;
    }

    /*private static void multiplePull(int clientNumber, String userSentence, boolean md5SumDefined) {
        Logger.consoleDebugLog("fire multiplePull");

        String fileName = SentenceUtils.getFileName(userSentence);
        if (FileList.getFileNameList(clientNumber).contains(fileName)) {
            Logger.consoleLog("You have file " + fileName + " in your local directory." +
                    " If you want download it, move or remove file from your directory.");
        } else {
            Socket connectionSocket = TCPConnectionUtils.createSocket(Config.HOST_IP, Config.PORT_NR);

            DataOutputStream outToServer = TCPConnectionUtils.getDataOutputStream(connectionSocket);
            String command;
            if (md5SumDefined) {
                command = String.valueOf(ServerCommand.CLIENTS_WHO_SHARING_SPECIFIC_FILE);
            } else {
                command = String.valueOf(ServerCommand.CLIENTS_WHO_SHARING_FILE);
            }
            fileName = SentenceUtils.getFileName(userSentence);
            if (SentenceUtils.getSentenceSize(userSentence) > 3) {
                String fileMD5Sum = SentenceUtils.getMD5Sum(userSentence);
                TCPConnectionUtils.writeSegmentToDataOutputStream(outToServer,
                        command,
                        String.valueOf(clientNumber),
                        fileName,
                        fileMD5Sum);
            } else {
                TCPConnectionUtils.writeSegmentToDataOutputStream(outToServer,
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
                Logger.consoleDebugLog(serverFileListSize + " users have file " + fileName);

                List<Integer> usersWithFile = new ArrayList<>();

                for (int i = 0; i < serverFileListSize; i++) {
                    String user = TCPConnectionUtils.readBufferedReaderLine(inFromServer);
                    usersWithFile.add(Integer.parseInt(user));
                    Logger.consoleDebugLog(user);
                }
                usersWithFile.remove(new Integer(clientNumber));

                TCPConnectionUtils.closeSocket(connectionSocket);

                if (usersWithFile.size() == 1) {
                    connectionSocket = TCPConnectionUtils.createSocket(Config.HOST_IP,
                            Config.PORT_NR + usersWithFile.get(0));

                    DataOutputStream outToClient = TCPConnectionUtils.getDataOutputStream(connectionSocket);
                    command = String.valueOf(ClientCommand.PUSH_ON_DEMAND);
                    fileName = SentenceUtils.getFileName(userSentence);
                    TCPConnectionUtils.writeSegmentToDataOutputStream(outToClient, command, String.valueOf(clientNumber), fileName);

                    TCPConnectionUtils.closeSocket(connectionSocket);

                    Logger.consoleLog("Sending push request");
                } else {
                    int clientWithFile = (!usersWithFile.stream().findFirst().isPresent() ? 0 :
                            usersWithFile.stream().findFirst().get());
                    if (clientWithFile == 0) {
                        Logger.consoleLog("No one share a " + fileName + " file");
                    } else {
                        connectionSocket = TCPConnectionUtils.createSocket(Config.HOST_IP, Config.PORT_NR + clientWithFile);

                        DataOutputStream outToClient = TCPConnectionUtils.getDataOutputStream(connectionSocket);
                        command = String.valueOf(ClientCommand.CLIENT_FILE_INFO);
                        TCPConnectionUtils.writeSegmentToDataOutputStream(outToClient,
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
                            Logger.clientLog("File downloaded successfully");
                        } else {
                            Logger.clientLog("Unsuccessful file download");
                            multiplePull(clientNumber, userSentence, md5SumDefined);
                        }
                    }
                }
            }

            TCPConnectionUtils.closeSocket(connectionSocket);
        }
    }

    private static void close(int clientNumber) {
        Logger.consoleDebugLog("fire close");

        Socket connectionSocket = TCPConnectionUtils.createSocket(Config.HOST_IP, Config.PORT_NR);

        DataOutputStream outToServer = TCPConnectionUtils.getDataOutputStream(connectionSocket);
        String command = String.valueOf(ServerCommand.UNREGISTER);
        TCPConnectionUtils.writeSegmentToDataOutputStream(outToServer, command, String.valueOf(clientNumber));

        BufferedReader inFromServer = TCPConnectionUtils.getBufferedReader(connectionSocket);
        TCPConnectionUtils.readBufferedReaderLine(inFromServer);

        TCPConnectionUtils.closeSocket(connectionSocket);

        Logger.consoleLog("Connection closed");
    }*/ // TODO turn on this
}
