package app.utils;

import java.io.IOException;
import java.net.Socket;

public class ConnectionUtils {

    public static Socket createSocket(String host, int port) {
        Socket hostClientSocket = null;
        try {
            hostClientSocket = new Socket(host, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return hostClientSocket;
    }

    public static void closeSocket(Socket hostClientSocket) {
        try {
            hostClientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
