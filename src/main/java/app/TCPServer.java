package app;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TCPServer extends Thread {

    private static final String SERVER_TAG = "<SERVER>: ";
    private List<FileInfo> serverFileInfoList;

    public TCPServer() {

        serverFileInfoList = new ArrayList<>();

    }

    public void run() {
        String clientSentence = null;
        String responseClientSentence;
        ServerSocket welcomeSocket = null;
        try {
            welcomeSocket = new ServerSocket(Config.PORT_NR);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            Socket connectionSocket = null;
            try {
                connectionSocket = Objects.requireNonNull(welcomeSocket).accept();
            } catch (IOException e) {
                e.printStackTrace();
            }

            BufferedReader inFromClient = null;
            try {
                inFromClient = new BufferedReader(new InputStreamReader(Objects.requireNonNull(connectionSocket).getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            DataOutputStream outToClient = null;
            try {
                outToClient = new DataOutputStream(Objects.requireNonNull(connectionSocket).getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            while (!Objects.requireNonNull(welcomeSocket).isClosed()) {

                try {
                    clientSentence = Objects.requireNonNull(inFromClient).readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (clientSentence != null) {
                    String[] sentences = clientSentence.split("\\*");
                    String command = sentences[0];

                    if (!command.equals(Command.REQUEST_FILES_LIST.name())) {
                        clientSentence = sentences[1];
                    }

                    if (command.equals(Command.CONNECT.name())) {
                        clientSentence += " - connected";

                        responseClientSentence = clientSentence + '\n';
                        try {
                            Objects.requireNonNull(outToClient).writeBytes(responseClientSentence);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if (command.equals(Command.SEND_FILES_LIST.name())) {
                        serverFileInfoList.add(FileList.unpackFileInfo(clientSentence));
                    }

                    if (command.equals(Command.REQUEST_FILES_LIST.name())) {
                        try {
                            Objects.requireNonNull(outToClient).writeBytes("" + serverFileInfoList.size() + '\n'); // Send list size
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        List<String> readyToSendList = FileList.packFileInfoList(serverFileInfoList);

                        DataOutputStream finalOutToClient = outToClient;
                        readyToSendList.forEach(
                                fileData -> {
                                    fileData = Command.REQUEST_FILES_LIST + "*" + fileData;
                                    try {
                                        Objects.requireNonNull(finalOutToClient).writeBytes(fileData + '\n');
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