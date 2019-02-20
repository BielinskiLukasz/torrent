package app.utils.connectionUtils;

import app.config.Config;

public class Segment {

    private static final String SPLITS_CHAR = Config.SPLITS_CHAR;
    private static final String EMPTY_STRING = "";

    private String sourceClient = EMPTY_STRING;
    private String destinationClient = EMPTY_STRING;
    private String command = EMPTY_STRING;
    private String fileName = EMPTY_STRING;
    private String fileSize = EMPTY_STRING;
    private String md5Sum = EMPTY_STRING;
    private String sequenceNumber = EMPTY_STRING;
    private String startByteNumber = EMPTY_STRING;
    private String endByteNumber = EMPTY_STRING;
    private String listSize = EMPTY_STRING;
    private String flag = EMPTY_STRING;
    private String message = EMPTY_STRING;
    private String comment = EMPTY_STRING;

    private Segment() {
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public static Segment unpack(String packedSentence) {
        String[] splitSegment = packedSentence.split(String.format("\\%s", Config.SPLITS_CHAR));

        Segment segment = new Segment();
        segment.sourceClient = splitSegment[0];
        segment.destinationClient = splitSegment[1];
        segment.command = splitSegment[2];
        segment.fileName = splitSegment[3];
        segment.fileSize = splitSegment[4];
        segment.md5Sum = splitSegment[5];
        segment.sequenceNumber = splitSegment[6];
        segment.startByteNumber = splitSegment[7];
        segment.endByteNumber = splitSegment[8];
        segment.listSize = splitSegment[9];
        segment.flag = splitSegment[10];
        segment.message = splitSegment[11];
        segment.comment = splitSegment[12];

        return segment;
    }

    public int getSourceClient() {
        return Integer.parseInt(sourceClient);
    }

    public int getDestinationClient() {
        return Integer.parseInt(destinationClient);
    }

    public String getCommand() {
        return command;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return Long.parseLong(fileSize);
    }

    public String getMd5Sum() {
        return md5Sum;
    }

    public int getSequenceNumber() {
        return Integer.parseInt(sequenceNumber);
    }

    public long getStartByteNumber() {
        return Long.parseLong(startByteNumber);
    }

    public long getEndByteNumber() {
        return Long.parseLong(endByteNumber);
    }

    public int getListSize() {
        return Integer.parseInt(listSize);
    }

    public boolean getFlag() {
        return flag.equalsIgnoreCase("true");
    }

    public String getMessage() {
        return message;
    }

    public String getComment() {
        return comment;
    }

    public String pack() {
        return sourceClient + SPLITS_CHAR +
                destinationClient + SPLITS_CHAR +
                command + SPLITS_CHAR +
                fileName + SPLITS_CHAR +
                fileSize + SPLITS_CHAR +
                md5Sum + SPLITS_CHAR +
                sequenceNumber + SPLITS_CHAR +
                startByteNumber + SPLITS_CHAR +
                endByteNumber + SPLITS_CHAR +
                listSize + SPLITS_CHAR +
                flag + SPLITS_CHAR +
                message + SPLITS_CHAR +
                comment;
    }

    @Override
    public String toString() {
        return "Segment{" +
                "sourceClient=" + sourceClient +
                ", destinationClient=" + destinationClient +
                ", command='" + command + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileSize=" + fileSize +
                ", md5Sum='" + md5Sum + '\'' +
                ", sequenceNumber=" + sequenceNumber +
                ", startByteNumber=" + startByteNumber +
                ", endByteNumber=" + endByteNumber +
                ", listSize=" + listSize +
                ", flag=" + flag +
                ", message='" + message + '\'' +
                ", comment='" + comment + '\'' +
                '}';
    }

    public static class Builder {

        private Segment segment;

        Builder() {
            segment = new Segment();
        }

        public Builder setSourceClient(int sourcePort) {
            segment.sourceClient = String.valueOf(sourcePort);
            return this;
        }

        public Builder setDestinationClient(int destinationPort) {
            segment.destinationClient = String.valueOf(destinationPort);
            return this;
        }

        public Builder setCommand(String command) {
            segment.command = command;
            return this;
        }

        public Builder setFileName(String fileName) {
            segment.fileName = fileName;
            return this;
        }

        public Builder setFileSize(long fileSize) {
            segment.fileSize = String.valueOf(fileSize);
            return this;
        }

        public Builder setMd5Sum(String md5Sum) {
            segment.md5Sum = md5Sum;
            return this;
        }

        public Builder setSequenceNumber(int sequenceNumber) {
            segment.sequenceNumber = String.valueOf(sequenceNumber);
            return this;
        }

        public Builder setStartByteNumber(long startByteNumber) {
            segment.startByteNumber = String.valueOf(startByteNumber);
            return this;
        }

        public Builder setEndByteNumber(long endByteNumber) {
            segment.endByteNumber = String.valueOf(endByteNumber);
            return this;
        }

        public Builder setListSize(int listSize) {
            segment.listSize = String.valueOf(listSize);
            return this;
        }

        public Builder setFlag(boolean flag) {
            segment.flag = String.valueOf(flag);
            return this;
        }

        public Builder setMessage(String message) {
            segment.message = message;
            return this;
        }

        public Builder setComment(String comment) {
            segment.comment = comment;
            return this;
        }

        public Segment build() {
            return segment;
        }
    }
}
