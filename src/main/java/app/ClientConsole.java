package app;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class ClientConsole extends Thread {

    private InputStream cmd;
    private TCPClient tcpClient;

    ClientConsole(TCPClient tcpClient) {
        this.tcpClient = tcpClient;
    }

    public void run() {
        while (true) {
            try {
                cmd = System.in;
                byte[] b = new byte[0];
                cmd.read(b);

                if (b.length > 0)
                    tcpClient.perform(Arrays.toString(b));

            } catch (IOException e) {
                System.out.println("Error: " + e);
                this.interrupt();
            }
        }

    }

}
