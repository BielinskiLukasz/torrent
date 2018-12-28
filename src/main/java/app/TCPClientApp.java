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

        System.out.println("TCPClientApp: run"); // TODO debug log

        while (true) {
//            System.out.println("TCPClientApp: while try11"); // TODO debug log
            try {
//                System.out.println("TCPClientApp: while try12"); // TODO debug log
                inFromUser = new BufferedReader(new InputStreamReader(System.in));
//                System.out.println("TCPClientApp: while try13"); // TODO debug log
                userSentence = inFromUser.readLine();

//                System.out.println("TCPClientApp: while try14"); // TODO debug log
                TCPClientAppAction.perform(client.clientNumber, userSentence);
//                System.out.println("TCPClientApp: while try15"); // TODO debug log

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

//            System.out.println("TCPClientApp: while..."); // TODO debug log <-- not visible
        }
    }
}
