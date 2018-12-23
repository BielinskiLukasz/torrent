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
    private ClientConsole console;
    private DataOutputStream outToServer;
    private BufferedReader inFromServer;
    private Socket clientSocket;

    public TCPClient(int clientNumber) {

        this.clientNumber = clientNumber;
        this.clientTag = "CLIENT_" + clientNumber + ": ";

        System.out.println(clientTag + "Client " + clientNumber + " created"); // TODO Tests

        clientSocket = null;
        outToServer = null;
        inFromServer = null;

        console = new ClientConsole(this);
        console.run();
    }

    public void run() {

        try {
            clientSocket = new Socket(Config.HOST_IP, Config.PORT_NR);
            outToServer = new DataOutputStream(Objects.requireNonNull(clientSocket).getOutputStream());
            inFromServer = new BufferedReader(new InputStreamReader(Objects.requireNonNull(clientSocket).getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }


        // TODO implements waiting for command and then do actions!!!

        while (clientNumber == 1) { // TODO refactor while condition
            // TODO get command if sent
            String command = null;

            perform(command);
        }


    }

    void perform(String command) {

        switch (Command.valueOf(command)) {
            case CONNECT:
                checkConnectionMessage(clientNumber, Objects.requireNonNull(outToServer));
                reCheckConnectionMessage(Objects.requireNonNull(inFromServer));
                break;

            case SEND_FILES_LIST:
                getFileInfoList(clientNumber);
                sendFileInfoListMessage(clientFileInfoList, outToServer);
                break;

            case REQUEST_FILES_LIST:
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
                break;

            case CLOSE:
                try {
                    outToServer.close();
                    inFromServer.close();
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

        }

    }

    private void getServerFileList(DataOutputStream outToServer) {
        String sentence = Command.REQUEST_FILES_LIST.name();
        System.out.println(clientTag + sentence); // TODO Tests
        try {
            outToServer.writeBytes(sentence + '\n');
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkConnectionMessage(int clientNumber, DataOutputStream outToServer) {
        String sentence = Command.CONNECT + "*" + "Client_" + clientNumber + " request connection with server";
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
                    fileData = Command.SEND_FILES_LIST + "*" + fileData;
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