package app.server;

import app.config.Config;
import app.utils.ExceptionHandler;
import app.utils.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class TCPServer extends Thread {

    private List<String> fileList;
    private final Set<Integer> userList;

    public TCPServer() {
        userList = new HashSet<>();
        fileList = new ArrayList<>();

        Logger.serverDebugLog("TCPServer: create server");
    }

    public void run() {
        Logger.serverDebugLog("TCPServer: run");
        Logger.serverLog("Start server");

        ServerSocket welcomeSocket = null;
        try {
            welcomeSocket = new ServerSocket(Config.PORT_NR);
        } catch (IOException e) {
            ExceptionHandler.handle(e);
        }

        while (true) {
            Socket connectionSocket;
            try {
                connectionSocket = Objects.requireNonNull(welcomeSocket).accept();
                TCPServerConnection connection = new TCPServerConnection(this, connectionSocket);
                connection.start();
            } catch (IOException e) {
                ExceptionHandler.handle(e);
            }

            try { // TODO sleep
                sleep(Config.MILLISECONDS_OF_CONNECTION_LISTENER_WAITING);
            } catch (InterruptedException e) {
                ExceptionHandler.handle(e);
            }
        }
    }

    void addClient(int clientNumber) {
        userList.add(clientNumber);
    }

    void removeClient(int clientNumber) {
        userList.remove(clientNumber);
    }

    Set<Integer> getUserList() {
        return userList;
    }

    void setFileList(List<String> fileList) {
        this.fileList = fileList;
    }

    boolean isClientConnected(int clientNumber) {
        return userList.contains(clientNumber);
    }
}