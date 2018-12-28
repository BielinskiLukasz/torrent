package app.client;

import app.Utils.FileInfo;
import app.client.console.TCPClientApp;
import app.client.host.TCPClientConnection;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.List;

public class TCPClient extends Thread {

    private int clientNumber;
    private static List<FileInfo> clientFileInfoList;

    private DataOutputStream outToServer;
    private BufferedReader inFromServer;
    private BufferedReader userCommand;
    private Socket clientSocket;

    public TCPClient(int clientNumber) {

        this.clientNumber = clientNumber;

        clientSocket = null;
        outToServer = null;
        inFromServer = null;
        userCommand = null;

        System.out.println("TCPClient: create client"); // TODO debug log
    }

    public void run() {
        TCPClientApp app = new TCPClientApp(this);
        TCPClientConnection connection = new TCPClientConnection(this);
        app.start();
        connection.start();

        System.out.println("TCPClient - run"); // TODO debug log

        /*try {
            clientSocket = new Socket(Config.HOST_IP, Config.PORT_NR);
            outToServer = new DataOutputStream(clientSocket.getOutputStream());
            inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            userCommand = new BufferedReader(new InputStreamReader(System.in));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            sentence = userCommand.readLine();
//            outToServer.writeBytes(sentence + '\n');
        } catch (IOException e) {
            e.printStackTrace();
        }

        perform(sentence);*/

    }

    public int getClientNumber() {
        return clientNumber;
    }
/*

    void perform(String command) {

        switch (CommandApp.valueOf(command)) {
            case CONNECT:
                checkConnectionMessage(clientNumber, outToServer);
                reCheckConnectionMessage(inFromServer);
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
        String sentence = CommandApp.REQUEST_FILES_LIST.name();
        try {
            outToServer.writeBytes(sentence + '\n');
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkConnectionMessage(int clientNumber, DataOutputStream outToServer) {
        String sentence = CommandApp.CONNECT + "*" + "Client_" + clientNumber + " request connection with server";
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
    }

    private void sendFileInfoListMessage(List<FileInfo> clientFileInfoList, DataOutputStream outToServer) {
        List<String> readyToSendList = FileList.packFileInfoList(clientFileInfoList);

        readyToSendList.forEach(
                fileData -> {
                    fileData = CommandApp.SEND_FILES_LIST + "*" + fileData;
                    try {
                        outToServer.writeBytes(fileData + '\n');
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        );

    }
*/

}