package app.client.console.multiHost;

import app.Utils.Logger;
import app.client.host.multiHost.TCPClientMH;
import app.config.Config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TCPConsoleConnectionMH extends Thread {

    private TCPClientMH client;

    public TCPConsoleConnectionMH(TCPClientMH tcpClientMH) {
        this.client = tcpClientMH;
    }

    public void run() {
        Logger.appDebugLog("TCPConsoleConnectionMH: run");

        BufferedReader inFromUser;
        String userSentence;

        while (true) {
            try {
                inFromUser = new BufferedReader(new InputStreamReader(System.in));
                userSentence = inFromUser.readLine();

                TCPConsoleActionMH.perform(client.getClientNumber(), userSentence);
            } catch (IOException e) {
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
