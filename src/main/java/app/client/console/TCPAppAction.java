package app.client.console;

import app.Utils.ActionUtils;
import app.Utils.Config;
import app.server.CommandServer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

class TCPAppAction {

    static void perform(int clientNumber, String userSentence) {

        String command = ActionUtils.getCommand(userSentence);

        switch (getCommandApp(command)) {
            case FILES_LIST:
                Socket connectionSocket;
                DataOutputStream outToServer = null;
                BufferedReader inFromServer = null;
                try {
                    connectionSocket = new Socket(Config.HOST_IP, Config.PORT_NR);
                    outToServer = new DataOutputStream(connectionSocket.getOutputStream());
                    inFromServer = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                } catch (IOException e) {
                    System.out.println("TCPAppAction - creating socket, outputStream and inputBufferedReader " + e);
                    e.printStackTrace();
                }

                System.out.println(command + " output: " + "no message"); // TODO debug log
                try {
                    outToServer.writeBytes(CommandServer.FILES_LIST + "\n");
                } catch (IOException e) {
                    System.out.println("TCPAppAction - write to server " + e);
                    e.printStackTrace();
                }

                String response = null;
                try {
                    response = inFromServer.readLine();
                } catch (IOException e) {
                    System.out.println("TCPAppAction - read from server " + e);
                    e.printStackTrace();
                }
                System.out.println(command + " input: " + response); // TODO debug log
                break;

            case EMPTY_COMMAND:
                break;

            case UNSUPPORTED_COMMAND:
            default:
                System.out.println('"' + command + '"' + " command is not supported"); // TODO debug log
                break;
        }
    }

    private static CommandApp getCommandApp(String command) {
        switch (command.trim().toUpperCase()) {
            case "LIST":
            case "FILES":
            case "FILES_LIST":
            case "FILES LIST":
            case "FILESLIST":
                return CommandApp.FILES_LIST;

            case "":
                return CommandApp.EMPTY_COMMAND;

            default:
                return CommandApp.UNSUPPORTED_COMMAND;
        }
    }
}
