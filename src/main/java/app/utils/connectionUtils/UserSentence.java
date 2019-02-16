package app.utils.connectionUtils;

import app.config.Config;

public class UserSentence {

    private static final String SPLITS_CHAR = Config.SPLITS_CHAR;

    private String userCommand;
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
                userSentence.userCommand = sentenceElements[0];
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

    public String getUserCommand() {
        return userCommand;
    }

    public String getFileName() {
        return fileName;
    }

    public String getTargetClient() {
        return targetClient;
    }


}
