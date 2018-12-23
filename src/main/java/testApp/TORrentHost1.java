package testApp;

import H2H.Host;

public class TORrentHost1 {

    public static void main(String[] args) {

        new Thread(() -> {
            try {
                Host host1 = new Host(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

    }

}
