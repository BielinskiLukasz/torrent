package app.utils;

import app.config.Config;

public class ActionUtils {

    public static String getCommand(String sentence) {
        return splitSentence(sentence)[0];
    }

    public static String getMessage(String sentence) {
        return splitSentence(sentence)[2];
    }

    public static String getFileName(String sentence) {
        return splitSentence(sentence)[2];
    }

    public static int getClientNumber(String sentence) {
        return Integer.parseInt(splitSentence(sentence)[1]);
    }

    public static int getListSize(String sentence) {
        return Integer.parseInt(splitSentence(sentence)[1]);
    }

    public static Boolean getBoolean(String sentence) {
        return Boolean.parseBoolean(splitSentence(sentence)[1]);
    }

    private static String[] splitSentence(String sentence) {
        return sentence.split("\\" + Config.SPLITS_CHAR);
    }
}
