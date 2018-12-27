package app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TCPClientApp extends Thread {

    private TCPClient client;

    TCPClientApp(TCPClient tcpClient) {
        this.client = tcpClient;
    }

    public void run() {
        BufferedReader inFromUser;
        String userSentence;

        while (true) {
            try {
                inFromUser = new BufferedReader(new InputStreamReader(System.in));
                userSentence = inFromUser.readLine();

                TCPClientAppAction.perform(client.clientNumber, userSentence);

            } catch (IOException e) {
                System.out.println("TCPClientApp - user command " + e);
                e.printStackTrace();
                this.interrupt();
            }

            try { // TODO sleep
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
