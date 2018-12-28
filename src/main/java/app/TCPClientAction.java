package app;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

class TCPClientAction {

    static void perform(int clientNumber, Socket connectionSocket, String clientSentence) throws IOException {
        DataOutputStream outToServer = new DataOutputStream(connectionSocket.getOutputStream());
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

        String command = ActionUtils.getCommand(clientSentence);

        switch (CommandClient.valueOf(command)) {
            case CONNECT:
                String message = ActionUtils.getMessage(clientSentence);
                System.out.println(command + " message: " + message); // TODO debug log
//                System.out.println(connectionSocket.toString()); // TODO debug log
                outToServer.writeBytes(CommandServer.CONNECT + Config.SENTENCE_SPLITS_CHAR + message + "\n");
                String helloResponse = inFromServer.readLine();
                System.out.println(command + " response: " + helloResponse); // TODO debug log
                break;
            default:
                break;
        }
    }
}
