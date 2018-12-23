package app;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.List;

public class TCPClient {

    private int clientNumber;
    private static List<FileInfo> clientFileInfoList;
    private String clientTag;

    public TCPClient(int clientNumber) throws IOException {

        this.clientNumber = clientNumber;
        this.clientTag = "CLIENT_" + clientNumber + ": ";

        System.out.println(clientTag + "Client " + clientNumber + " created"); // TODO Tests

        Socket clientSocket = new Socket(Config.HOST_IP, Config.PORT_NR);

        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        checkConnectionMessage(clientNumber, outToServer);
        reCheckConnectionMessage(inFromServer);

        getFileInfoList(clientNumber);
        sendFileInfoListMessage(clientFileInfoList, outToServer);

        getServerFileList(outToServer);
        int serverListSize = Integer.parseInt(inFromServer.readLine());
        System.out.println(serverListSize); // TODO Tests
        for (int i = 0; i < serverListSize; i++) {
            reCheckConnectionMessage(inFromServer);
        }

        clientSocket.close();

    }

    private void getServerFileList(DataOutputStream outToServer) throws IOException {
        String sentence = Command.SERVER_FILES_LIST.name();
        System.out.println(clientTag + sentence); // TODO Tests
        outToServer.writeBytes(sentence + '\n');
    }

    private void checkConnectionMessage(int clientNumber, DataOutputStream outToServer) throws IOException {
        String sentence = Command.REGISTER + "*" + "Client_" + clientNumber + " request connection with server";
        System.out.println(clientTag + sentence); // TODO Tests
        outToServer.writeBytes(sentence + '\n');
    }

    private void reCheckConnectionMessage(BufferedReader inFromServer) throws IOException {
        String modifiedSentence = inFromServer.readLine();
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