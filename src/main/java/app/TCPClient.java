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
    private BufferedReader userCommand;
    private Socket clientSocket;
    private String sentence;

    public TCPClient(int clientNumber) {

        this.clientNumber = clientNumber;
        this.clientTag = "CLIENT_" + clientNumber + ": ";

        clientSocket = null;
        outToServer = null;
        inFromServer = null;
        userCommand = null;
    }

    public void run() {

        try {
            clientSocket = new Socket(Config.HOST_IP, Config.PORT_NR);
            outToServer = new DataOutputStream(Objects.requireNonNull(clientSocket).getOutputStream());
            inFromServer = new BufferedReader(new InputStreamReader(Objects.requireNonNull(clientSocket).getInputStream()));
            userCommand = new BufferedReader(new InputStreamReader(System.in));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            sentence = userCommand.readLine();
            outToServer.writeBytes(sentence + '\n');
        } catch (IOException e) {
            e.printStackTrace();
        }

        perform(sentence);

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
        try {
            outToServer.writeBytes(sentence + '\n');
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkConnectionMessage(int clientNumber, DataOutputStream outToServer) {
        String sentence = Command.CONNECT + "*" + "Client_" + clientNumber + " request connection with server";
        try {
            outToServer.writeBytes(sentence + '\n');
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void reCheckConnectionMessage(BufferedReader inFromServer) {

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
                    try {
                        outToServer.writeBytes(fileData + '\n');
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        );

    }

}