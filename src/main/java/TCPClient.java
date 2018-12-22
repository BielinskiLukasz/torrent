import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

class TCPClient {

    private int clientNumber;

    TCPClient(int clientNumber) throws IOException {

        this.clientNumber = clientNumber;
        System.out.println("Client " + clientNumber + " created");

        String sentence;
        String modifiedSentence;
        Socket clientSocket = new Socket(Config.HOST_IP, Config.PORT_NR);

//        BufferedReader inFromUser =
//                new BufferedReader(new InputStreamReader(System.in));
        DataOutputStream outToServer =
                new DataOutputStream(clientSocket.getOutputStream());
        BufferedReader inFromServer =
                new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

//        sentence = inFromUser.readLine();

        sentence = "Client_" + clientNumber + " request connection with server";
        System.out.println(sentence);

        outToServer.writeBytes(sentence + '\n');
        modifiedSentence = inFromServer.readLine();
        System.out.println("CLIENT_" + clientNumber + ": " + modifiedSentence);
        clientSocket.close();

    }


    public static void main(String[] argv) {

    }
}