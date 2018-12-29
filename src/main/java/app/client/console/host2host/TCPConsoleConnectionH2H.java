package app.client.console.host2host;

import app.Utils.Logger;
import app.client.host.host2host.TCPClientH2H;
import app.config.Config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TCPConsoleConnectionH2H extends Thread {

    private TCPClientH2H client;

    public TCPConsoleConnectionH2H(TCPClientH2H tcpClientH2H) {
        this.client = tcpClientH2H;
    }

    public void run() {
        Logger.appDebugLog("TCPConsoleConnectionH2H: run");

        BufferedReader inFromUser;
        String userSentence;

        while (true) {
            try {
                inFromUser = new BufferedReader(new InputStreamReader(System.in));
                userSentence = inFromUser.readLine();

                TCPConsoleActionH2H.perform(client.getClientNumber(),
                        userSentence,
                        Config.PORT_NR + client.getConnectedClientNumber());
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
