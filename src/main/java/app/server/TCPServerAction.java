package app.server;

import app.Utils.ActionUtils;
import app.Utils.Config;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

class TCPServerAction {

    static void perform(TCPServer server, Socket connectionSocket, String clientSentence) {
        String command = ActionUtils.getCommand(clientSentence);

        switch (CommandServer.valueOf(command)) {
            case CONNECT:
                connect(server, connectionSocket, clientSentence);
                break;

            case FILES_LIST:
                System.out.println(command + " input: " + "no message"); // TODO debug log

                List<String> serverFileList = new ArrayList<>();

                server.getUserList().forEach(
                        userNumber -> {
                            Socket userSocket = null;
                            try {
                                userSocket = new Socket(Config.HOST_IP, Config.PORT_NR + userNumber);
                            } catch (IOException e) {
                                System.out.println(
                                        "TCPServerAction - creating socket " + e);
                                e.printStackTrace();
                            }

                            DataOutputStream outToClient = null;
                            try {
                                outToClient = new DataOutputStream(userSocket.getOutputStream());
                            } catch (IOException e) {
                                System.out.println(
                                        "TCPServerAction - creating dataOutputStream " + e);
                                e.printStackTrace();
                            }

                            System.out.println(command + " output: " + "no message"); // TODO debug log
                            try {
                                outToClient.writeBytes(command + "\n");
                            } catch (IOException e) {
                                System.out.println("TCPServerAction - write to client " + e);
                                e.printStackTrace();
                            }

                            BufferedReader inFromClient = null;
                            try {
                                inFromClient = new BufferedReader(new InputStreamReader(userSocket.getInputStream()));
                            } catch (IOException e) {
                                System.out.println(
                                        "TCPServerAction - creating inputBufferedReader " + e);
                                e.printStackTrace();
                            }

                            String response = null;
                            try {
                                response = inFromClient.readLine();
                            } catch (IOException e) {
                                System.out.println("TCPServerAction - read from client (clientFileList size) " + e);
                                e.printStackTrace();
                            }
                            System.out.println(command + " input: " + response); // TODO debug log

                            // TODO implement action adding files to fileList
                            int clientFileListSize = ActionUtils.getListSize(response);
                            for (int i = 0; i < clientFileListSize; i++) {
                                try {
                                    serverFileList.add(
                                            inFromClient.readLine()
                                    );
                                } catch (IOException e) {
                                    System.out.println("TCPServerAction - read from client (specific clientFile) " + e);
                                    e.printStackTrace();
                                }
                            }

                            try {
                                userSocket.close();
                            } catch (IOException e) {
                                System.out.println("Error: " + e);
                            }
                        }
                );

                server.setFileList(serverFileList);

                // TODO implement sending fileList to client (who asks)
                DataOutputStream outToClient = null;
                try {
                    outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                } catch (IOException e) {
                    System.out.println("TCPServerAction - creating outputStream " + e);
                    e.printStackTrace();
                }

                String response = String.valueOf(serverFileList.size()); // TODO rename string - size of list ???
                System.out.println(command + " output: " + response); // TODO debug log
                try {
                    outToClient.writeBytes(command + Config.SENTENCE_SPLITS_CHAR + response + "\n");
                } catch (IOException e) {
                    System.out.println("TCPClientAction - write to server " + e);
                    e.printStackTrace();
                }

                DataOutputStream finalOutToServer = outToClient;
                serverFileList.forEach(
                        fileData -> {
                            try {
                                finalOutToServer.writeBytes(fileData + "\n");
                            } catch (IOException e) {
                                System.out.println("TCPClientAction - write to server (specific clientFile)" + e);
                                e.printStackTrace();
                            }
                            System.out.println(command + " input: " + fileData); // TODO debug log
                        }
                );

                break;

            default:
                sendNotSupportedCommandMessage(connectionSocket, command);
                break;
        }

        /*String responseClientSentence;

        DataOutputStream outToClient = null;
        try {
            outToClient = new DataOutputStream(connectionSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (clientSentence != null) {
            String[] sentences = clientSentence.split("\\*");
            String command = sentences[0];

            if (!command.equals(CommandApp.REQUEST_FILES_LIST.name())) {
                clientSentence = sentences[1];
            }

            if (command.equals(CommandApp.CONNECT.name())) {
                clientSentence += " - connected";

                responseClientSentence = clientSentence + '\n';
                try {
                    outToClient.writeBytes(responseClientSentence);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (command.equals(CommandApp.SEND_FILES_LIST.name())) {
                server.fileInfoList.add(FileList.unpackFileInfo(clientSentence));
            }

            if (command.equals(CommandApp.REQUEST_FILES_LIST.name())) {
                try {
                    outToClient.writeBytes("" + server.fileInfoList.size() + '\n'); // Send list size
                } catch (IOException e) {
                    e.printStackTrace();
                }
                List<String> readyToSendList = FileList.packFileInfoList(server.fileInfoList);

                DataOutputStream finalOutToClient = outToClient;
                readyToSendList.forEach(
                        fileData -> {
                            fileData = CommandApp.REQUEST_FILES_LIST + "*" + fileData;
                            try {
                                finalOutToClient.writeBytes(fileData + '\n');
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                );
            }
        }*/
    }

    private static void connect(TCPServer server, Socket connectionSocket, String clientSentence) {
        DataOutputStream outToClient = null;
        try {
            outToClient = new DataOutputStream(connectionSocket.getOutputStream());
        } catch (IOException e) {
            System.out.println("TCPServerAction - creating outputStream " + e);
            e.printStackTrace();
        }

        String command = ActionUtils.getCommand(clientSentence);
        String message = ActionUtils.getMessage(clientSentence);
        int clientNumber = ActionUtils.getClientNumber(clientSentence);
        System.out.println(command + " input: " + message); // TODO debug log

        server.addClient(clientNumber);

        String response = "Hello client " + clientNumber;
        System.out.println(command + " output: " + response); // TODO debug log

        try {
            outToClient.writeBytes(response + "\n");
        } catch (IOException e) {
            System.out.println("TCPClientAction - write to server " + e);
            e.printStackTrace();
        }

        System.out.println("Connection to client " + clientNumber + " was detected");
    }

    private static void sendNotSupportedCommandMessage(Socket connectionSocket, String command) {
        DataOutputStream outToClient = null;
        try {
            outToClient = new DataOutputStream(connectionSocket.getOutputStream());
        } catch (IOException e) {
            System.out.println("TCPServerAction - creating outputStream " + e);
            e.printStackTrace();
        }

        String response = '"' + command + '"' + " command is not supported yet";
        System.out.println(command + " output: " + response); // TODO debug log

        try {
            outToClient.writeBytes(response + "\n");
        } catch (IOException e) {
            System.out.println("TCPClientAction - write to server " + e);
            e.printStackTrace();
        }
    }
}
