package app;

import java.io.IOException;
import java.io.InputStream;

public class ClientConsole extends Thread {

    private InputStream cmd;

    public void run() {
        while (true) {
            try {
                cmd = System.in;
                byte[] b = new byte[0];
                cmd.read(b);


            } catch (IOException e) {
                System.out.println("Error: " + e);
                this.interrupt();
            }
        }

    }

}
