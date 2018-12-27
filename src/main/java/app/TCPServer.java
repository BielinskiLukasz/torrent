package app;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class TCPServer extends Thread {

    List<FileInfo> fileInfoList;
    private List<String> userList;

    public TCPServer() {
        userList = new ArrayList<>();
        fileInfoList = new ArrayList<>();
    }

    public void run() {
        ServerSocket welcomeSocket = null;

        try {
            welcomeSocket = new ServerSocket(Config.PORT_NR);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            Socket connectionSocket;

            try {
                connectionSocket = welcomeSocket.accept();
                TCPServerConnection connection = new TCPServerConnection(this, connectionSocket);
                connection.run();
            } catch (IOException e) {
                System.out.println("Server connection " + e);
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