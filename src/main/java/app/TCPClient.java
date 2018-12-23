package app;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.List;
import java.util.Objects;

public class TCPClient extends Thread {

    private int clientNumber;
    private static List<FileInfo> clientFileInfoList;
    private String clientTag;

    public TCPClient(int clientNumber) {

        this.clientNumber = clientNumber;
        this.clientTag = "CLIENT_" + clientNumber + ": ";

        System.out.println(clientTag + "Client " + clientNumber + " created"); // TODO Tests

    }

    public void run() {

        Socket clientSocket = null;
        DataOutputStream outToServer = null;
        BufferedReader inFromServer = null;

        try {
            clientSocket = new Socket(Config.HOST_IP, Config.PORT_NR);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            outToServer = new DataOutputStream(Objects.requireNonNull(clientSocket).getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            inFromServer = new BufferedReader(new InputStreamReader(Objects.requireNonNull(clientSocket).getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        checkConnectionMessage(clientNumber, Objects.requireNonNull(outToServer));
        reCheckConnectionMessage(Objects.requireNonNull(inFromServer));

        getFileInfoList(clientNumber);
        sendFileInfoListMessage(clientFileInfoList, outToServer);

        getServerFileList(outToServer);
        int serverListSize = 0;

        try {
            serverListSize = Integer.parseInt(inFromServer.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(serverListSize); // TODO Tests
        for (int i = 0; i < serverListSize; i++) {
            reCheckConnectionMessage(inFromServer);
        }

        try {
            outToServer.close();
            inFromServer.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void getServerFileList(DataOutputStream outToServer) {
        String sentence = Command.SERVER_FILES_LIST.name();
        System.out.println(clientTag + sentence); // TODO Tests
        try {
            outToServer.writeBytes(sentence + '\n');
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkConnectionMessage(int clientNumber, DataOutputStream outToServer) {
        String sentence = Command.REGISTER + "*" + "Client_" + clientNumber + " request connection with server";
        System.out.println(clientTag + sentence); // TODO Tests
        try {
            outToServer.writeBytes(sentence + '\n');
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void reCheckConnectionMessage(BufferedReader inFromServer) {
        String modifiedSentence = null;
        try {
            modifiedSentence = inFromServer.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(clientTag + modifiedSentence); // TODO Tests
    }

    private void getFileInfoList(int clientNumber) {
        clientFileInfoList = FileList.getFileInfoList(clientNumber);
//        FileList.testGetFileInfoListResults(clientFileInfoList);
    }

    private void sendFileInfoListMessage(List<FileInfo> clientFileInfoList, DataOutputStream outToServer) {
        List<String> readyToSendList = FileList.packFileInfoList(clientFileInfoList);

        readyToSendList.forEach(
                fileData -> {
                    fileData = Command.CLIENT_FILES_LIST + "*" + fileData;
                    System.out.println(clientTag + fileData); // TODO Tests
                    try {
                        outToServer.writeBytes(fileData + '\n');
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        );

    }

}