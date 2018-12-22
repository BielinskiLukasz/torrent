import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

class TCPClient {

    public static void main(String[] argv) throws Exception {

        String sentence;
        String modifiedSentence;
        Socket clientSocket = new Socket(Congif.HOST_IP, Congif.PORT_NR);

        BufferedReader inFromUser =
                new BufferedReader(new InputStreamReader(System.in));
        DataOutputStream outToServer =
                new DataOutputStream(clientSocket.getOutputStream());
        BufferedReader inFromServer =
                new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        sentence = inFromUser.readLine();
        outToServer.writeBytes(sentence + '\n');
        modifiedSentence = inFromServer.readLine();
        System.out.println("From TCPServer: " + modifiedSentence);
        clientSocket.close();
    }
}