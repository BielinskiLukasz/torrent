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
        System.out.println(SERVER_TAG + "Server created"); // TODO Tests

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

                try { // TODO Tests
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                try {
                    clientSentence = Objects.requireNonNull(inFromClient).readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (clientSentence != null) {
                    String[] sentences = clientSentence.split("\\*");
                    String command = sentences[0];
                    System.out.println(command); // TODO Tests

                    if (!command.equals(Command.SERVER_FILES_LIST.name())) {
                        clientSentence = sentences[1];
                        System.out.println(SERVER_TAG + clientSentence); // TODO Tests
                    }

                    if (command.equals(Command.REGISTER.name())) {
                        clientSentence += " - connected";

                        responseClientSentence = clientSentence + '\n';
                        try {
                            Objects.requireNonNull(outToClient).writeBytes(responseClientSentence);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if (command.equals(Command.CLIENT_FILES_LIST.name())) {
                        serverFileInfoList.add(FileList.unpackFileInfo(clientSentence));
                    }

                    if (command.equals(Command.SERVER_FILES_LIST.name())) {
                        try {
                            Objects.requireNonNull(outToClient).writeBytes("" + serverFileInfoList.size() + '\n'); // Send list size
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        List<String> readyToSendList = FileList.packFileInfoList(serverFileInfoList);

                        DataOutputStream finalOutToClient = outToClient;
                        readyToSendList.forEach(
                                fileData -> {
                                    fileData = Command.SERVER_FILES_LIST + "*" + fileData;
                                    System.out.println(SERVER_TAG + fileData); // TODO Tests
                                    try {
                                        Objects.requireNonNull(finalOutToClient).writeBytes(fileData + '\n');
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                        );
                    }

                    if (command.equals(Command.PULL.name())) {

                    }

                }
            }

            System.out.println(SERVER_TAG + "inner while out");

        }

    }

}