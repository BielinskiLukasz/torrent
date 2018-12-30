package app.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ConnectionUtils {

    public static Socket createSocket(String host, int port) {
        try {
            return new Socket(host, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void closeSocket(Socket socket) {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static DataOutputStream getDataOutputStream(Socket socket) {
        try {
            return new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static BufferedReader getBufferedReader(Socket socket) {
        try {
            return new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
