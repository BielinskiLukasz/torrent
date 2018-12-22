public class TORrent {

    public static void main(String[] args) {

        new Thread(() -> {
            try {
                TCPServer server = new TCPServer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                TCPClient client1 = new TCPClient(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                TCPClient client2 = new TCPClient(2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

    }

}
