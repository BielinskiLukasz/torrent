package app;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

class TCPServerAction {

    static void perform(TCPServer server, Socket connectionSocket, String clientSentence) { // TODO refactor method

        String responseClientSentence;

        DataOutputStream outToClient = null;
        try {
            outToClient = new DataOutputStream(connectionSocket.getOutputStream());
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
                    outToClient.writeBytes(responseClientSentence);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (command.equals(Command.SEND_FILES_LIST.name())) {
                server.fileInfoList.add(FileList.unpackFileInfo(clientSentence));
            }

            if (command.equals(Command.REQUEST_FILES_LIST.name())) {
                try {
                    outToClient.writeBytes("" + server.fileInfoList.size() + '\n'); // Send list size
                } catch (IOException e) {
                    e.printStackTrace();
                }
                List<String> readyToSendList = FileList.packFileInfoList(server.fileInfoList);

                DataOutputStream finalOutToClient = outToClient;
                readyToSendList.forEach(
                        fileData -> {
                            fileData = Command.REQUEST_FILES_LIST + "*" + fileData;
                            try {
                                finalOutToClient.writeBytes(fileData + '\n');
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                );
            }

        }

    }

}
