package app.client.console.multiHost;

import app.client.host.ClientCommand;
import app.client.host.multiHost.TCPClientConnectionActionMH;
import app.config.Config;
import app.utils.Logger;
import app.utils.connectionUtils.SentenceUtils;
import app.utils.connectionUtils.TCPConnectionUtils;
import app.utils.fileUtils.MD5Sum;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.Socket;
import java.util.List;

public class MultipleDownloadManager extends Thread {

    private int clientNumber;
    private String fileName;
    private int userWithFile;
    private int position;
    private long stepSize;

    private List<Integer> usersWithFile;

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
            Logger.clientDebugLog("File part downloaded successfully");
        } else {
            Logger.clientDebugLog("Unsuccessful file part download");
            invokeRePushPart(connectionSocket, packetNumber, file.length(), 0);
            // TODO inform about possibility of destroy sourcePart of file
            if (MD5Sum.check(targetPath, filePartMD5Sum)) {
                Logger.clientDebugLog("File part downloaded successfully");
            } else {
                while (!MD5Sum.check(targetPath, filePartMD5Sum) && usersWithFile.size() > 1) {
                    Logger.clientDebugLog("Unsuccessful file part download");
                    usersWithFile.remove(userWithFile);
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

                    invokeRePushPart(connectionSocket, packetNumber, file.length(), 0);
                    // TODO inform about possibility of destroy sourcePart of file
                }
            }
        }

        //TODO implement restart send part (after sender check request with also need be implemented here)

        Logger.clientDebugLog(command + " downloading parts sequence ended");

        TCPConnectionUtils.closeSocket(connectionSocket);

        Logger.consoleDebugLog(startByteNum + " " + endByteNum);
    }

    private void invokeRePushPart(Socket connectionSocket, long packetNumber, long receivedFilePartSize, int reconnectCounter) {
        Logger.clientDebugLog("fire invokeRePushPart");

        String command = String.valueOf(ClientCommand.REPUSH);
        int sourceClientNumber = userWithFile;
        String partFileName = fileName + ".part_" + packetNumber;
        String clientSentence = command + Config.SPLITS_CHAR + sourceClientNumber + Config.SPLITS_CHAR + partFileName +
                Config.SPLITS_CHAR + receivedFilePartSize;

        Logger.clientDebugLog("sentence: " + clientSentence);
        TCPClientConnectionActionMH.invokeRepush(clientNumber,
                connectionSocket,
                clientSentence,
                reconnectCounter);
    }
    //TODO rename all repush to rePush and repull to rePull
}
