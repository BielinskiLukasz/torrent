package app;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class TCPServer {

    private static final String SERVER_TAG = "<SERVER>: ";
    private List<FileInfo> serverFileInfoList;

    public TCPServer() throws IOException {

        serverFileInfoList = new ArrayList<>();
        System.out.println(SERVER_TAG + "Server created"); // TODO Tests

        String clientSentence;
        String responseClientSentence;
        ServerSocket welcomeSocket = new ServerSocket(Config.PORT_NR);

        while (true) {
            Socket connectionSocket = welcomeSocket.accept();

            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

            while (!connectionSocket.isClosed()) {
                clientSentence = inFromClient.readLine();
                if (clientSentence != null) {
                    String[] sentences = clientSentence.split("\\*");
                    String command = sentences[0];
                    System.out.println(command); // TODO Tests
                    if (!command.equals(Command.SERVER_FILES_LIST.toString())) {
                        clientSentence = sentences[1];
                        System.out.println(SERVER_TAG + clientSentence); // TODO Tests
                    }

                    if (command.equals(Command.REGISTER.toString())) {
                        clientSentence += " - connected";

                        responseClientSentence = clientSentence + '\n';
                        outToClient.writeBytes(responseClientSentence);
                    }

                    if (command.equals(Command.CLIENT_FILES_LIST.toString())) {
                        serverFileInfoList.add(FileList.unpackFileInfo(clientSentence));
                    }

                    if (command.equals(Command.SERVER_FILES_LIST.toString())) {
                        outToClient.writeBytes("" + serverFileInfoList.size() + '\n'); // Send list size
                        List<String> readyToSendList = FileList.packFileInfoList(serverFileInfoList);

                        readyToSendList.forEach(
                                fileData -> {
                                    fileData = Command.SERVER_FILES_LIST + "*" + fileData;
                                    System.out.println(SERVER_TAG + fileData); // TODO Tests
                                    try {
                                        outToClient.writeBytes(fileData + '\n');
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                        );
                    }
                }
            }
        }

    }

}