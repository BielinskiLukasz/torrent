package app.utils.connectionUtils;

import app.config.Config;
import app.utils.ExceptionHandler;
import app.utils.Logger;

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

public class TCPConnectionUtils {

    public static Socket createSocket(String host, int port) {
        Socket socket = null;
        try {
            socket = new Socket(host, port);
        } catch (IOException e) {
            ExceptionHandler.handle(e);
        }

        Logger.utilsDebugLog("createSocket");
        return socket;
    }

    public static void closeSocket(Socket socket) {
        try {
            socket.close();
        } catch (IOException e) {
            ExceptionHandler.handle(e);
        }

        Logger.utilsDebugLog("closeSocket");
    }

    public static DataOutputStream getDataOutputStream(Socket socket) {
        DataOutputStream dataOutputStream = null;
        try {
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            ExceptionHandler.handle(e);
        }

        Logger.utilsDebugLog("getDataOutputStream");
        return dataOutputStream;
    }

    public static BufferedReader getBufferedReader(Socket socket) {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            ExceptionHandler.handle(e);
        }

        Logger.utilsDebugLog("getBufferedReader");
        return bufferedReader;
    }

    public static OutputStream getOutputStream(Socket connectionSocket) {
        OutputStream outputStream = null;
        try {
            outputStream = connectionSocket.getOutputStream();
        } catch (IOException e) {
            ExceptionHandler.handle(e);
        }

        Logger.utilsDebugLog("getOutputStream");
        return outputStream;
    }

    public static InputStream getInputStream(Socket connectionSocket) {
        InputStream inputStream = null;
        try {
            inputStream = connectionSocket.getInputStream();
        } catch (IOException e) {
            ExceptionHandler.handle(e);
        }

        Logger.utilsDebugLog("getInputStream");
        return inputStream;
    }

    public static FileOutputStream createFileOutputStream(File file, boolean append) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file, append);
        } catch (FileNotFoundException e) {
            ExceptionHandler.handle(e);
        }

        Logger.utilsDebugLog("createFileOutputStream");
        return fileOutputStream;
    }

    public static FileOutputStream createFileOutputStream(File file) {
        return createFileOutputStream(file, false);
    }

    public static void closeFileOutputStream(FileOutputStream fileOutputStream) {
        try {
            fileOutputStream.close();
        } catch (IOException e) {
            ExceptionHandler.handle(e);
        }

        Logger.utilsDebugLog("closeFileOutputStream");
    }

    public static FileInputStream createFileInputStream(File file) {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            ExceptionHandler.handle(e);
        }

        Logger.utilsDebugLog("createFileInputStream");
        return fileInputStream;
    }

    public static void closeFileInputStream(FileInputStream fileInputStream) {
        try {
            fileInputStream.close();
        } catch (IOException e) {
            ExceptionHandler.handle(e);
        }

        Logger.utilsDebugLog("closeFileInputStream");
    }

    public static void writeMessageToDataOutputStream(DataOutputStream dataOutputStream, String message, String... furtherPartOfMessage) {
        if (furtherPartOfMessage.length > 0) {
            message += createAttachedMessage(furtherPartOfMessage);
        }

        try {
            dataOutputStream.writeBytes(message + "\n");
        } catch (IOException e) {
            ExceptionHandler.handle(e);
        }

        Logger.utilsDebugLog("writeMessageToDataOutputStream");
        Logger.utilsDebugLog("send: " + message); // TODO refactor/delete (don't need to add split chars here)
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
            ExceptionHandler.handle(e);
        }

        Logger.utilsDebugLog("readBufferedReaderLine");
        Logger.serverDebugLog("read: " + response);
        return response;
    }

    public static void writeFileToStream(FileInputStream fileInputStream, OutputStream outputStream) {
        Logger.serverDebugLog("start sending file");

        int count;
        byte[] buffer = new byte[Config.BUFFER_SIZE_IN_BYTES];
        try {
            while ((count = fileInputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, count);
            }
        } catch (IOException e) {
            ExceptionHandler.handle(e);
        }

        Logger.serverDebugLog("finish sending file");
    }

    public static void readFileFromStream(FileOutputStream fileOutputStream, InputStream inputStream) {
        Logger.serverDebugLog("start read file");

        int count;
        byte[] buffer = new byte[8192];
        try {
            while ((count = inputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, count);
            }
        } catch (IOException e) {
            ExceptionHandler.handle(e);
        }

        Logger.serverDebugLog("finish read file");
    }
}
