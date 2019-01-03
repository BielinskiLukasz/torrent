package app.utils;

import app.client.console.ConsoleCommand;
import app.config.Config;

import java.util.Objects;

public class SentenceUtils {

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
        return getElement(sentence, 2);
    }

    public static String getFileName(String sentence) {
        return getElement(sentence, 2);
    }

    public static int getClientNumber(String sentence) {
        return Integer.parseInt(Objects.requireNonNull(getElement(sentence, 1)));
    }

    public static int getListSize(String sentence) {
        return Integer.parseInt(Objects.requireNonNull(getElement(sentence, 1)));
    }

    static Boolean getBoolean(String sentence) {
        return Boolean.parseBoolean(getElement(sentence, 1));
    }

    public static String getMD5Sum(String sentence) {
        return getElement(sentence, 3);
    }

    public static long getStartByteNumber(String sentence) {
        return Long.parseLong(Objects.requireNonNull(getElement(sentence, 3)));
    }

    private static String getElement(String sentence, int elementId) {
        try {
            return splitSentence(sentence)[elementId];
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
        return null;
    }

    private static String[] splitSentence(String sentence) {
        return sentence.split(String.format("\\%s", Config.SPLITS_CHAR));
    }

    public static String cleanUserSentence(String userSentence) {
        if (!userSentence.contains(Config.SPLITS_CHAR)) {
            userSentence = addSplitChars(userSentence);
        }
        return userSentence.replaceAll("\"", "");
    }

    private static String addSplitChars(String userSentence) {
        for (int i = 0; i < (Config.MAX_NUMBER_OF_PARAMETERS - 1); i++) {
            userSentence = userSentence.replaceFirst(" ", Config.SPLITS_CHAR);
        }

        return userSentence;
    }

    private static boolean isClientNumberInSentence(String userSentence) {
        return splitSentence(userSentence).length > 1 &&
                isInteger(splitSentence(userSentence)[1]) &&
                Integer.parseInt(splitSentence(userSentence)[1]) >= 0;
    }

    private static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException | NullPointerException e) {
            return false;
        }
        return true;
    }

    public static String setClientNumber(String clientSentence, int clientNumber) {
        if (!isClientNumberInSentence(clientSentence))
            clientSentence = clientSentence.replaceFirst(String.format("\\%s", Config.SPLITS_CHAR),
                    Config.SPLITS_CHAR + clientNumber + Config.SPLITS_CHAR);
        return clientSentence;
    }
}
