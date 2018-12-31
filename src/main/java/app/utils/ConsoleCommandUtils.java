package app.utils;

import app.client.console.ConsoleCommand;
import app.config.Config;

public class ConsoleCommandUtils {

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
                return String.valueOf(ConsoleCommand.FILE_LIST);
            case "PULL":
            case "D":
            case "DOWNLOAD":
                return String.valueOf(ConsoleCommand.PULL);
            case "PUSH":
            case "U":
            case "UPLOAD":
                return String.valueOf(ConsoleCommand.PUSH);
            case "C":
            case "CLOSE":
            case "E":
            case "EXIT":
            case "Q":
            case "QUIT":
            case "UNREGISTER":
                return String.valueOf(ConsoleCommand.CLOSE);
            case "":
                return String.valueOf(ConsoleCommand.EMPTY_COMMAND);
            default:
                return String.valueOf(ConsoleCommand.UNSUPPORTED_COMMAND);
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
        return sentence.split(String.format("\\%s", Config.SPLITS_CHAR));
    }
}
