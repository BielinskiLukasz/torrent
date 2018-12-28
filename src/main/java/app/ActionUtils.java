package app;

class ActionUtils {

    static String getCommand(String clientSentence) {
        String[] sentences = clientSentence.split("\\" + Config.SENTENCE_SPLITS_CHAR);
        return sentences[0];
    }

    static String getMessage(String clientSentence) {
        String[] sentences = clientSentence.split("\\" + Config.SENTENCE_SPLITS_CHAR);
        return sentences[1];
    }
}