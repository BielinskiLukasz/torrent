package app.Utils;

public class ActionUtils {

    public static String getCommand(String clientSentence) {
        return splitSentence(clientSentence)[0];
    }

    public static String getMessage(String clientSentence) {
        return splitSentence(clientSentence)[1];
    }

    public static int getClientNumber(String clientSentence) {
        return Integer.parseInt(splitSentence(clientSentence)[2]);
    }

    public static int getListSize(String response) {
        return Integer.parseInt(splitSentence(response)[1]);
    }

    private static String[] splitSentence(String clientSentence) {
        return clientSentence.split("\\" + Config.SENTENCE_SPLITS_CHAR);
    }
}
