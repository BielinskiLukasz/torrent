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

        System.out.println(clientTag + "Client " + clientNumber + " created");

        Socket clientSocket = new Socket(Config.HOST_IP, Config.PORT_NR);

        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        checkConnectionMessage(clientNumber, outToServer);
        reCheckConnectionMessage(inFromServer);

        getFileInfoList(clientNumber);
        sendFileInfoListMessage(clientFileInfoList, outToServer);

        clientSocket.close();

    }

    private void checkConnectionMessage(int clientNumber, DataOutputStream outToServer) throws IOException {
        String sentence = "Client_" + clientNumber + " request connection with server";
        System.out.println(clientTag + sentence);
        outToServer.writeBytes(sentence + '\n');
    }

    private void reCheckConnectionMessage(BufferedReader inFromServer) throws IOException {
        String modifiedSentence = inFromServer.readLine();
        System.out.println(clientTag + modifiedSentence);
    }

    private void getFileInfoList(int clientNumber) {
        clientFileInfoList = FileList.getFileInfoList(clientNumber);
//        FileList.testGetFileInfoListResults(clientFileInfoList);
    }

    private void sendFileInfoListMessage(List<FileInfo> clientFileInfoList, DataOutputStream outToServer) {
        List<String> readyToSendList = FileList.preparingFileInfoListToSend(clientFileInfoList);

        readyToSendList.forEach(
                fileData -> {
                    try {
                        System.out.println(clientTag + fileData);
                        outToServer.writeBytes(fileData + '\n');
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        );

    }

}