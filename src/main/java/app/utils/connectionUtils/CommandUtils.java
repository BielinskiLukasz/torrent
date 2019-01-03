package app.utils.connectionUtils;

import app.client.console.ConsoleCommand;

public class CommandUtils {

    public static String getCommand(String sentence) {
        return SentenceUtils.splitSentence(sentence)[0];
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
            case "MULTIPLE_PULL":
            case "MULTIPLE PULL":
            case "MULTIPLEPULL":
            case "MULTI_PULL":
            case "MULTI PULL":
            case "MULTIPULL":
            case "M_PULL":
            case "M PULL":
            case "MPULL":
            case "MP":
                return String.valueOf(ConsoleCommand.MULTIPLE_PULL);
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

    public static String transformToH2HCommand(String command) {
        return command.equals("FILE_LIST") ? "CLIENT_FILE_LIST" : command;
    }
}
