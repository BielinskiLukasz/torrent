package app.client.console;

import app.Utils.Logger;
import app.client.TCPClient;
import app.config.Config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TCPClientApp extends Thread {

    private TCPClient client;

    public TCPClientApp(TCPClient tcpClient) {
        this.client = tcpClient;
    }

    public void run() {
        Logger.appDebugLog("TCPClientApp: run");

        BufferedReader inFromUser;
        String userSentence;

        while (true) {
            try {
                inFromUser = new BufferedReader(new InputStreamReader(System.in));
                userSentence = inFromUser.readLine();

                TCPAppAction.perform(client.getClientNumber(), userSentence);
            } catch (IOException e) {
                System.out.println("TCPClientApp - read and perform user command " + e);
                e.printStackTrace();
                this.interrupt();
            }

            try { // TODO sleep
                sleep(Config.MILLISECONDS_OF_CONNECTION_LISTENER_WAITING);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
