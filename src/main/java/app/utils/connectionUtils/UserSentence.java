package app.utils.connectionUtils;

import app.client.console.ConsoleCommand;
import app.config.Config;

public class UserSentence {

    private static final String SPLITS_CHAR = Config.SPLITS_CHAR;

    private ConsoleCommand userCommand;
    private String fileName;
    private String targetClient;

    private UserSentence() {
    }

    public static UserSentence getUserSentence(String sentenceFromConsole) {
        UserSentence userSentence = new UserSentence();

        if (isSeparateWithSpaces(sentenceFromConsole)) {
            sentenceFromConsole = separateWithSplitChar(sentenceFromConsole);
        }

        String[] sentenceElements = sentenceFromConsole.split(String.format("\\%s", SPLITS_CHAR));
        switch (sentenceElements.length) {
            case 3:
                userSentence.targetClient = sentenceElements[2];
            case 2:
                userSentence.fileName = sentenceElements[1];
            case 1:
                userSentence.userCommand = getCommandSupportedName(sentenceElements[0]);
            default:

        }

        return userSentence;
    }

    private static boolean isSeparateWithSpaces(String sentenceFromConsole) {
        return !sentenceFromConsole.contains(":");
    }

    private static String separateWithSplitChar(String sentenceFromConsole) {
        return sentenceFromConsole.replace(" ", SPLITS_CHAR);
    }

    private static ConsoleCommand getCommandSupportedName(String command) {
        switch (command.trim().toUpperCase()) {
            case "CONNECT":
                return ConsoleCommand.CONNECT;
            case "L":
            case "LIST":
            case "FILES":
            case "F":
            case "FL":
            case "FILE_LIST":
            case "FILES LIST":
            case "FILESLIST":
            case "FILES_LIST":
            case "FILE LIST":
            case "FILELIST":
                return ConsoleCommand.FILE_LIST;
            case "PULL":
            case "PL":
            case "D":
            case "DOWNLOAD":
                return ConsoleCommand.PULL;
            case "PUSH":
            case "PS":
            case "U":
            case "UPLOAD":
            case "SEND":
            case "S":
                return ConsoleCommand.PUSH;
            case "MULTIPLE_PULL":
            case "MULTIPLE PULL":
            case "MULTIPLEPULL":
            case "MULTI_PULL":
            case "MULTI PULL":
            case "MULTIPULL":
            case "M_PULL":
            case "M PULL":
            case "MPULL":
            case "MPL":
            case "MP":
            case "M":
                return ConsoleCommand.MULTIPLE_PULL;
            case "C":
            case "CLOSE":
            case "E":
            case "EXIT":
            case "Q":
            case "QUIT":
            case "UNREGISTER":
                return ConsoleCommand.CLOSE;
            case "":
                return ConsoleCommand.EMPTY_COMMAND;
            default:
                return ConsoleCommand.UNSUPPORTED_COMMAND;
        }
    }

    public String getFileName() {
        return fileName;
    }

    public String getTargetClient() {
        return targetClient;
    }

    public ConsoleCommand getUserCommand() {
        return userCommand;
    }

    @Override
    public String toString() {
        return "UserSentence{" +
                "userCommand=" + userCommand.toString() +
                ", fileName='" + fileName + '\'' +
                ", targetClient='" + targetClient + '\'' +
                '}';
    }
}
