package app.client;

import app.client.console.ConsoleCommand;
import app.config.Config;
import app.utils.ConnectionUtils;
import app.utils.FileList;
import app.utils.Logger;
import app.utils.MD5Sum;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

public class ClienActionUtils {

    public static void uploadIfFileExist(int sourceClientNumber, int targetClientNumber, String fileName) {
        if (isClientHaveFile(sourceClientNumber, fileName)) {
            upload(sourceClientNumber, targetClientNumber, fileName);
        } else {
            Logger.consoleLog("You haven't selected file");
        }
    }

    private static void upload(int sourceClientNumber, int targetClientNumber, String fileName) {
        String filePath = Config.BASIC_PATH + sourceClientNumber + "//" + fileName;
        File file = new File(filePath);

        Socket hostConnectionSocket = ConnectionUtils.createSocket(Config.HOST_IP,
                Config.PORT_NR + targetClientNumber);

        DataOutputStream outToClient = ConnectionUtils.getDataOutputStream(hostConnectionSocket);
        String command = ConsoleCommand.PUSH.name();
        ConnectionUtils.sendMessageToDataOutputStream(outToClient, command, String.valueOf(sourceClientNumber), fileName);

        String md5sum = MD5Sum.md5(filePath);
        String response = "Sending file " + fileName + " md5 sum";
        ConnectionUtils.sendMessageToDataOutputStream(outToClient, command, md5sum, response);

        FileInputStream fileInputStream = ConnectionUtils.createFileInputStream(file);
        OutputStream outputStream = ConnectionUtils.getOutputStream(hostConnectionSocket);
        ConnectionUtils.sendFileByStream(fileInputStream, outputStream);
        ConnectionUtils.closeFileInputStream(fileInputStream);

        Logger.consoleLog("Send file " + fileName + " to client " + targetClientNumber);

        ConnectionUtils.closeSocket(hostConnectionSocket);

        Logger.consoleLog("Finished");
    }

    private static boolean isClientHaveFile(int clientNumber, String fileName) {
        List<String> clientFileNameList = FileList.getFileNameList(clientNumber);
        return clientFileNameList.contains(fileName);
    }
}
