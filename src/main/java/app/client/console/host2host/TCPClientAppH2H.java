package app.client.console.host2host;

import app.Utils.Logger;
import app.client.host.host2host.TCPClientH2H;
import app.config.Config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TCPClientAppH2H extends Thread {

    private TCPClientH2H client;

    public TCPClientAppH2H(TCPClientH2H tcpClientH2H) {
        this.client = tcpClientH2H;
    }

    public void run() {
        Logger.appDebugLog("TCPClientAppH2H: run");

        BufferedReader inFromUser;
        String userSentence;

        while (true) {
            try {
                inFromUser = new BufferedReader(new InputStreamReader(System.in));
                userSentence = inFromUser.readLine();

                TCPAppActionH2H.perform(client.getClientNumber(), userSentence);
            } catch (IOException e) {
                System.out.println("TCPClientAppH2H - read and perform user command " + e);
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
