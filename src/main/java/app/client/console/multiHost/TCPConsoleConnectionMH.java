package app.client.console.multiHost;

import app.client.host.multiHost.TCPClientMH;
import app.config.Config;
import app.utils.ExceptionHandler;
import app.utils.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TCPConsoleConnectionMH extends Thread {

    // TODO BACKLOG implements some connection protocol :)

    private final TCPClientMH client;

    public TCPConsoleConnectionMH(TCPClientMH tcpClientMH) {
        this.client = tcpClientMH;
    }

    public void run() {
        Logger.consoleDebugLog("TCPConsoleConnectionMH: run");

        BufferedReader inFromUser;
        String userSentence;

        while (true) {
            try {
                inFromUser = new BufferedReader(new InputStreamReader(System.in));
                userSentence = inFromUser.readLine();

                //TODO BACKLOG add regex of incoming user sentence

                TCPConsoleActionMH.perform(client.getClientNumber(), userSentence);
            } catch (IOException e) {
                ExceptionHandler.handle(e);
                this.interrupt(); // TODO BACKLOG if (regex is added) remove this line
            }

            try { // TODO sleep
                sleep(Config.MILLISECONDS_OF_CONNECTION_LISTENER_WAITING);
            } catch (InterruptedException e) {
                ExceptionHandler.handle(e);
            }
        }
    }
}
