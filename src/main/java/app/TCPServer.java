package app;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class TCPServer extends Thread {

    private List<FileInfo> fileInfoList;
    private List<String> userList;

    public TCPServer() {
        userList = new ArrayList<>();
        fileInfoList = new ArrayList<>();

        System.out.println("TCPServer: create server"); // TODO debug log
    }

    public void run() {
        ServerSocket welcomeSocket = null;

        System.out.println("TCPServer: run"); // TODO debug log

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
}