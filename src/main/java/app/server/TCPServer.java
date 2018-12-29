package app.server;

import app.Utils.Config;
import app.Utils.FileInfo;
import app.Utils.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TCPServer extends Thread {

    private List<FileInfo> fileInfoList;
    private List<String> fileList;
    private Set<Integer> userList;

    public TCPServer() {
        userList = new HashSet<>();
        fileList = new ArrayList<>();
        fileInfoList = new ArrayList<>();

        Logger.serverDebugLog("TCPServer: create server");
    }

    public void run() {
        Logger.serverDebugLog("TCPServer: run");

        ServerSocket welcomeSocket = null;
        try {
            welcomeSocket = new ServerSocket(Config.PORT_NR);
        } catch (IOException e) {
            System.out.println("TCPServer - new ServerSocket " + e);
            e.printStackTrace();
        }

        while (true) {
            Socket connectionSocket;
            try {
                connectionSocket = welcomeSocket.accept();
                TCPServerConnection connection = new TCPServerConnection(this, connectionSocket);
                connection.start();
            } catch (IOException e) {
                System.out.println("TCPServer - server connection " + e);
                e.printStackTrace();
            }

            try { // TODO sleep
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    void addClient(int clientNumber) {
        userList.add(clientNumber);
    }

    Set<Integer> getUserList() {
        return userList;
    }

    void setFileList(List<String> fileList) {
        this.fileList = fileList;
    }
}