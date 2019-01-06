package app.client.console.multiHost;

import app.client.host.ClientCommand;
import app.config.Config;
import app.utils.ExceptionHandler;
import app.utils.Logger;
import app.utils.connectionUtils.SentenceUtils;
import app.utils.connectionUtils.TCPConnectionUtils;
import app.utils.fileUtils.MD5Sum;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.List;

class MultipleDownloadManager extends Thread {

    private final int clientNumber;
    private final String fileName;
    private int userWithFile;
    private int position;
    private final long stepSize;

    private final List<Integer> usersWithFile;

    MultipleDownloadManager(int clientNumber,
                            String fileName,
                            int userWithFile,
                            int position,
                            long stepSize,
                            List<Integer> usersWithFile) {
        this.clientNumber = clientNumber;
        this.fileName = fileName;
        this.position = position;
        this.userWithFile = userWithFile;
        this.stepSize = stepSize;
        this.usersWithFile = usersWithFile;
    }

    public void run() {
        int packetNumber = position;
        long startByteNum = stepSize * position++;
        long endByteNum = stepSize * position - 1;
        // TODO BACKLOG ignore packet where endByte <= startByte (remove half of users/download all file from one user
        //  when file size is less than config.min)

        Socket connectionSocket = TCPConnectionUtils.createSocket(Config.HOST_IP, Config.PORT_NR + userWithFile);

        DataOutputStream outToClient = TCPConnectionUtils.getDataOutputStream(connectionSocket);
        String command = String.valueOf(ClientCommand.HANDLE_PUSH_PACK);
        TCPConnectionUtils.writeMessageToDataOutputStream(outToClient,
                command,
                String.valueOf(clientNumber),
                fileName,
                String.valueOf(startByteNum),
                String.valueOf(endByteNum),
                String.valueOf(packetNumber)); // TODO BACKLOG better send part size and packet number

        BufferedReader inFromClient = TCPConnectionUtils.getBufferedReader(connectionSocket);
        String sentence = TCPConnectionUtils.readBufferedReaderLine(inFromClient);
        String filePartMD5Sum = SentenceUtils.getMD5Sum(sentence);

        String filePath = Config.BASIC_PATH + clientNumber + "//" + fileName;
        String suffix = ".part_";
        String targetPath = filePath + suffix + packetNumber;

        File file = new File(targetPath);
        FileOutputStream fileOutputStream = TCPConnectionUtils.createFileOutputStream(file);
        InputStream inputStream = TCPConnectionUtils.getInputStream(connectionSocket);
        TCPConnectionUtils.readFileFromStream(fileOutputStream, inputStream);
        TCPConnectionUtils.closeFileOutputStream(fileOutputStream);

        if (MD5Sum.check(targetPath, filePartMD5Sum)) {
            Logger.clientDebugLog("File part " + targetPath + " downloaded successfully");
        } else {
            Logger.clientDebugLog("Unsuccessful file part " + targetPath + " download");


            long receivedFilePartSize = file.length();
            Logger.clientDebugLog("Downloaded " + receivedFilePartSize + " bytes");

            boolean reconnect = false;
            for (int i = 0; i < Config.WAITING_TIME_SEC; i++) {
                try {
                    Logger.clientDebugLog("Try reconnect with client " + userWithFile);
                    connectionSocket = new Socket(Config.HOST_IP, Config.PORT_NR + userWithFile);
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
                Logger.clientLog("Reconnect with client " + userWithFile);
                invokeRePullPart(connectionSocket, packetNumber, file.length());
            } else {
                Logger.clientLog("Cannot reconnect with client " + userWithFile);
            }

            // TODO inform about possibility of destroy sourcePart of file
            if (MD5Sum.check(targetPath, filePartMD5Sum)) {
                Logger.clientDebugLog("File part downloaded successfully");
            } else {
                while (!MD5Sum.check(targetPath, filePartMD5Sum) && usersWithFile.size() > 1) {
                    Logger.clientDebugLog("Unsuccessful file part download");
                    usersWithFile.remove(new Integer(userWithFile));
                    userWithFile = usersWithFile.get(0);

                    connectionSocket = TCPConnectionUtils.createSocket(Config.HOST_IP,
                            Config.PORT_NR + userWithFile);

                    command = String.valueOf(ClientCommand.CREATE_PART_OF_FILE);
                    outToClient = TCPConnectionUtils.getDataOutputStream(connectionSocket);
                    TCPConnectionUtils.writeMessageToDataOutputStream(outToClient,
                            command,
                            String.valueOf(clientNumber),
                            fileName,
                            String.valueOf(startByteNum),
                            String.valueOf(endByteNum),
                            String.valueOf(packetNumber));

                    TCPConnectionUtils.closeSocket(connectionSocket);
                    connectionSocket = TCPConnectionUtils.createSocket(Config.HOST_IP,
                            Config.PORT_NR + userWithFile);

                    invokeRePullPart(connectionSocket, packetNumber, file.length());
                    // TODO inform about possibility of destroy sourcePart of file
                }
            }
        }

        TCPConnectionUtils.closeSocket(connectionSocket);

        Logger.clientDebugLog(command + " downloading parts sequence ended");
        Logger.consoleDebugLog(startByteNum + " " + endByteNum);
    }

    private void invokeRePullPart(Socket connectionSocket,
                                  long packetNumber,
                                  long receivedFilePartSize) {
        Logger.clientDebugLog("fire invokeRePullPart");

        String command = String.valueOf(ClientCommand.RE_PULL);
        int sourceClientNumber = userWithFile;
        String partFileName = fileName + ".part_" + packetNumber;

        DataOutputStream outToClient = TCPConnectionUtils.getDataOutputStream(connectionSocket);
        TCPConnectionUtils.writeMessageToDataOutputStream(outToClient,
                command,
                "",
                partFileName,
                String.valueOf(receivedFilePartSize));

        BufferedReader inFromClient = TCPConnectionUtils.getBufferedReader(connectionSocket);
        String sentence = TCPConnectionUtils.readBufferedReaderLine(inFromClient);
        String fileMD5Sum = SentenceUtils.getMD5Sum(sentence);

        String filePath = Config.BASIC_PATH + clientNumber + "//" + partFileName;
        File file = new File(filePath);
        FileOutputStream fileOutputStream = TCPConnectionUtils.createFileOutputStream(file, true);
        InputStream inputStream = TCPConnectionUtils.getInputStream(connectionSocket);
        TCPConnectionUtils.readFileFromStream(fileOutputStream, inputStream);
        TCPConnectionUtils.closeFileOutputStream(fileOutputStream);

        if (MD5Sum.check(filePath, fileMD5Sum)) {
            Logger.clientLog("File downloaded successfully");
        } else {
            Logger.clientLog("Unsuccessful file download");
        }
    }
}


// TODO secure after sending client number when he ask about clients who have file (if he have inform him and don't send request to server)