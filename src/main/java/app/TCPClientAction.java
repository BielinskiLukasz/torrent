package app;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

class TCPClientAction {

    static void perform(int clientNumber, Socket connectionSocket, String clientSentence) {
        DataOutputStream outToServer = null;
        BufferedReader inFromServer = null;
        try {
            outToServer = new DataOutputStream(connectionSocket.getOutputStream());
            inFromServer = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        } catch (IOException e) {
            System.out.println("TCPClientAction - creating outputStream and inputBufferedReader " + e);
            e.printStackTrace();
        }

        String command = ActionUtils.getCommand(clientSentence);

        switch (CommandClient.valueOf(command)) {
            case CONNECT:
                String message = ActionUtils.getMessage(clientSentence);
                System.out.println(command + " message: " + message); // TODO debug log
//                System.out.println(connectionSocket.toString()); // TODO debug log
                try {
                    outToServer.writeBytes(CommandServer.CONNECT + Config.SENTENCE_SPLITS_CHAR + message + "\n");
                } catch (IOException e) {
                    System.out.println("TCPClientAction - write to server " + e);
                    e.printStackTrace();
                }

                String helloResponse = null;
                try {
                    helloResponse = inFromServer.readLine();
                } catch (IOException e) {
                    System.out.println("TCPClientAction - read from server " + e);
                    e.printStackTrace();
                }
                System.out.println(command + " response: " + helloResponse); // TODO debug log
                break;
            default:
                break;
        }
    }
}
