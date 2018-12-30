package app.utils;

import app.client.console.ConsoleCommand;
import app.config.Config;

public class ActionUtils {

    public static String getCommand(String sentence) {
        return splitSentence(sentence)[0];
    }

    public static String getConsoleCommand(String sentence) {
        return getCommandSupportedName(getCommand(sentence));
    }

    private static String getCommandSupportedName(String command) {
        switch (command.trim().toUpperCase()) {
            case "L":
            case "LIST":
            case "FILES":
            case "FL":
            case "FILE_LIST":
            case "FILES LIST":
            case "FILESLIST":
            case "FILES_LIST":
            case "FILE LIST":
            case "FILELIST":
                return ConsoleCommand.FILE_LIST.name();
            case "C":
            case "CLOSE":
            case "E":
            case "EXIT":
            case "Q":
            case "QUIT":
                return ConsoleCommand.CLOSE.name();
            case "PULL":
            case "D":
            case "DOWNLOAD":
                return ConsoleCommand.PULL.name();
            case "PUSH":
            case "U":
            case "UPLOAD":
                return ConsoleCommand.PUSH.name();
            case "":
                return ConsoleCommand.EMPTY_COMMAND.name();
            default:
                return ConsoleCommand.UNSUPPORTED_COMMAND.name();
        }
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

    public static String getMD5Sum(String sentence) {
        return splitSentence(sentence)[1];
    }

    private static String[] splitSentence(String sentence) {
        return sentence.split("\\" + Config.SPLITS_CHAR);
    }
}
