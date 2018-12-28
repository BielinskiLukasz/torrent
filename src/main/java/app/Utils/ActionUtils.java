package app.Utils;

public class ActionUtils {

    public static String getCommand(String clientSentence) {
        String[] sentences = clientSentence.split("\\" + Config.SENTENCE_SPLITS_CHAR);
        return sentences[0];
    }

    public static String getMessage(String clientSentence) {
        String[] sentences = clientSentence.split("\\" + Config.SENTENCE_SPLITS_CHAR);
        return sentences[1];
    }

    public static int getClientNumber(String clientSentence) {
        String[] sentences = clientSentence.split("\\" + Config.SENTENCE_SPLITS_CHAR);
        return Integer.parseInt(sentences[2]);
    }
}
