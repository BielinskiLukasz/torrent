package app.server;

import app.Utils.ActionUtils;
import app.client.host.CommandClient;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

class TCPServerAction {

    static void perform(TCPServer server, Socket connectionSocket, String clientSentence) { // TODO refactor method
        DataOutputStream outToClient = null;
        try {
            outToClient = new DataOutputStream(connectionSocket.getOutputStream());
        } catch (IOException e) {
            System.out.println("TCPServerAction - creating outputStream " + e);
            e.printStackTrace();
        }

        String command = ActionUtils.getCommand(clientSentence);

        switch (CommandClient.valueOf(command)) {
            case CONNECT:
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
                break;
            default:
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
}
