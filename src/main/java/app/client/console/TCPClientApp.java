package app.client.console;

import app.client.TCPClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TCPClientApp extends Thread {

    private TCPClient client;

    public TCPClientApp(TCPClient tcpClient) {
        this.client = tcpClient;
    }

    public void run() {
        BufferedReader inFromUser;
        String userSentence;

        System.out.println("TCPClientApp: run"); // TODO debug log

        while (true) {
            try {
                inFromUser = new BufferedReader(new InputStreamReader(System.in));
                userSentence = inFromUser.readLine();

                TCPAppAction.perform(client.getClientNumber(), userSentence);
            } catch (IOException e) {
                System.out.println("TCPClientApp - rea and perform user command " + e);
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
