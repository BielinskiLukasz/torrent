package app.utils.connectionUtils;

import app.config.Config;

public class Segment {

    private static final String SPLITS_CHAR = Config.SPLITS_CHAR;
    private static final String EMPTY_STRING = "";
    private static final int EMPTY_INT = -1;

    private int sourceClient = EMPTY_INT;
    private int destinationClient = EMPTY_INT;
    private String command = EMPTY_STRING;
    private String fileName = EMPTY_STRING;
    private int fileSize = EMPTY_INT;
    private String md5Sum = EMPTY_STRING;
    private int sequenceNumber = EMPTY_INT;
    private int startByteNumber = EMPTY_INT;
    private int endByteNumber = EMPTY_INT;
    private int listSize = EMPTY_INT;
    private int flag = EMPTY_INT;
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
        segment.sourceClient = unpackInt(splitSegment[0]);
        segment.destinationClient = unpackInt(splitSegment[1]);
        segment.command = splitSegment[2];
        segment.fileName = splitSegment[3];
        segment.fileSize = unpackInt(splitSegment[4]);
        segment.md5Sum = splitSegment[5];
        segment.sequenceNumber = unpackInt(splitSegment[6]);
        segment.startByteNumber = unpackInt(splitSegment[7]);
        segment.endByteNumber = unpackInt(splitSegment[8]);
        segment.listSize = unpackInt(splitSegment[9]);
        segment.flag = unpackInt(splitSegment[10]);
        segment.message = splitSegment[11];
        segment.comment = splitSegment[12];

        return segment;
    }

    private static int unpackInt(String packedInt) {
        return packedInt.equals(EMPTY_STRING) ? EMPTY_INT : Integer.parseInt(packedInt);
    }

    public int getSourceClient() {
        return sourceClient;
    }

    public int getDestinationClient() {
        return destinationClient;
    }

    public String getCommand() {
        return command;
    }

    public String getFileName() {
        return fileName;
    }

    public int getFileSize() {
        return fileSize;
    }

    public String getMd5Sum() {
        return md5Sum;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public int getStartByteNumber() {
        return startByteNumber;
    }

    public int getEndByteNumber() {
        return endByteNumber;
    }

    public int getListSize() {
        return listSize;
    }

    public boolean getFlag() {
        return flag == 1;
    }

    public String getMessage() {
        return message;
    }

    public String getComment() {
        return comment;
    }

    public String pack() {
        return packInt(sourceClient) + SPLITS_CHAR +
                packInt(destinationClient) + SPLITS_CHAR +
                command + SPLITS_CHAR +
                fileName + SPLITS_CHAR +
                packInt(fileSize) + SPLITS_CHAR +
                md5Sum + SPLITS_CHAR +
                packInt(sequenceNumber) + SPLITS_CHAR +
                packInt(startByteNumber) + SPLITS_CHAR +
                packInt(endByteNumber) + SPLITS_CHAR +
                packInt(listSize) + SPLITS_CHAR +
                packInt(flag) + SPLITS_CHAR +
                message + SPLITS_CHAR +
                comment;
    }

    private String packInt(int num) {
        return num == EMPTY_INT ? EMPTY_STRING : String.valueOf(num);
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
            segment.sourceClient = sourcePort;
            return this;
        }

        public Builder setDestinationClient(int destinationPort) {
            segment.destinationClient = destinationPort;
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

        public Builder setFileSize(int fileSize) {
            segment.fileSize = fileSize;
            return this;
        }

        public Builder setMd5Sum(String md5Sum) {
            segment.md5Sum = md5Sum;
            return this;
        }

        public Builder setSequenceNumber(int sequenceNumber) {
            segment.sequenceNumber = sequenceNumber;
            return this;
        }

        public Builder setStartByteNumber(int startByteNumber) {
            segment.startByteNumber = startByteNumber;
            return this;
        }

        public Builder setEndByteNumber(int endByteNumber) {
            segment.endByteNumber = endByteNumber;
            return this;
        }

        public Builder setListSize(int listSize) {
            segment.listSize = listSize;
            return this;
        }

        public Builder setFlag(boolean flag) {
            segment.flag = flag ? 1 : 0;
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
