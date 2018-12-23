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

        clientSocket.close();

    }

    private void checkConnectionMessage(int clientNumber, DataOutputStream outToServer) throws IOException {
        String sentence = clientTag + "Client_" + clientNumber + " request connection with server";
        System.out.println(sentence);
        outToServer.writeBytes(sentence + '\n');
    }

    private void reCheckConnectionMessage(BufferedReader inFromServer) throws IOException {
        String modifiedSentence = inFromServer.readLine();
        System.out.println(clientTag + modifiedSentence);
    }

    private void getFileInfoList(int clientNumber) {
        clientFileInfoList = FileList.getFileInfoList(clientNumber);
        FileList.testGetFileInfoListResults(clientFileInfoList);
    }

}