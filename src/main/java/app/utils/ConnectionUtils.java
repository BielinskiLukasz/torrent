package app.utils;

import app.config.Config;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class ConnectionUtils {

    public static Socket createSocket(String host, int port) {
        Socket socket = null;
        try {
            socket = new Socket(host, port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Logger.utilsDebugLog("createSocket");
        return socket;
    }

    public static void closeSocket(Socket socket) {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Logger.utilsDebugLog("closeSocket");
    }

    public static DataOutputStream getDataOutputStream(Socket socket) {
        DataOutputStream dataOutputStream = null;
        try {
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Logger.utilsDebugLog("getDataOutputStream");
        return dataOutputStream;
    }

    public static BufferedReader getBufferedReader(Socket socket) {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Logger.utilsDebugLog("getBufferedReader");
        return bufferedReader;
    }

    public static OutputStream getOutputStream(Socket connectionSocket) {
        OutputStream outputStream = null;
        try {
            outputStream = connectionSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Logger.utilsDebugLog("getOutputStream");
        return outputStream;
    }

    public static InputStream getInputStream(Socket connectionSocket) {
        InputStream inputStream = null;
        try {
            inputStream = connectionSocket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Logger.utilsDebugLog("getInputStream");
        return inputStream;
    }

    public static FileOutputStream createFileOutputStream(File file) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Logger.utilsDebugLog("createFileOutputStream");
        return fileOutputStream;
    }

    public static void closeFileOutputStream(FileOutputStream fileOutputStream) {
        try {
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Logger.utilsDebugLog("closeFileOutputStream");
    }

    public static FileInputStream createFileInputStream(File file) {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Logger.utilsDebugLog("createFileInputStream");
        return fileInputStream;
    }

    public static void closeFileInputStream(FileInputStream fileInputStream) {
        try {
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Logger.utilsDebugLog("closeFileInputStream");
    }

    public static void sendMessageToDataOutputStream(DataOutputStream dataOutputStream, String message, String... furtherPartOfMessage) {
        if (furtherPartOfMessage.length > 0) {
            message += createAttachedMessage(furtherPartOfMessage);
        }

        try {
            dataOutputStream.writeBytes(message + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Logger.utilsDebugLog("send: " + message);
    }

    private static String createAttachedMessage(String[] furtherPartsOfMessage) {
        StringBuilder attachedMessage = new StringBuilder();

        for (String furtherPartOfMessage : furtherPartsOfMessage) {
            attachedMessage.append(Config.SPLITS_CHAR).append(furtherPartOfMessage);
        }

        Logger.utilsDebugLog("createAttachedMessage");
        return attachedMessage.toString();
    }

    public static String readBufferedReaderLine(BufferedReader bufferedReader) {
        String response = null;
        try {
            response = bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Logger.serverDebugLog("get: " + response);
        return response;
    }
}
