package app.client.host.multiHost;

import app.client.host.ClientCommand;
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

public class MultipleSender extends Thread {

    private int clientNumber;
    private String fileName;
    private int userWithFile;
    private int position;
    private long stepSize;

    public MultipleSender(int clientNumber, String fileName, int userWithFile, int position, long stepSize) {
        this.clientNumber = clientNumber;
        this.fileName = fileName;
        this.position = position;
        this.userWithFile = userWithFile;
        this.stepSize = stepSize;
    }

    public void run() {
        int packetNumber = position;
        long startByteNum = stepSize * position++;
        long endByteNum = stepSize * position - 1;
        // TODO ignore packet where endByte <= startByte (remove half of users/download all file from one user
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
                String.valueOf(packetNumber)); // TODO better send part size and packet number

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
//                    invokeRepush(clientNumber, connectionSocket, clientSentence, 0); // TODO implements restart downloading part
        }

        Logger.clientDebugLog(command + " downloading parts sequence ended");

        TCPConnectionUtils.closeSocket(connectionSocket);

        Logger.consoleDebugLog(startByteNum + " " + endByteNum);
    }
}
