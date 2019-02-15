package app.utils.connectionUtils;

import app.config.Config;

public class Segment {

    private static final String SPLITS_CHAR = Config.SPLITS_CHAR;
    private static final String EMPTY_STRING = "";
    private static final int EMPTY_INT = -1;

    private String sourceIp = EMPTY_STRING;
    private String destinationIp = EMPTY_STRING;
    private int sourcePort = EMPTY_INT;
    private int destinationPort = EMPTY_INT;
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

    public static Segment unpackSegment(String packedSentence) {
        String[] splitSegment = packedSentence.split(String.format("\\%s", Config.SPLITS_CHAR));

        Segment segment = new Segment();
        segment.sourceIp = splitSegment[0];
        segment.destinationIp = splitSegment[1];
        segment.sourcePort = Integer.parseInt(splitSegment[2]);
        segment.destinationPort = Integer.parseInt(splitSegment[3]);
        segment.command = splitSegment[4];
        segment.fileName = splitSegment[5];
        segment.fileSize = Integer.parseInt(splitSegment[6]);
        segment.md5Sum = splitSegment[7];
        segment.sequenceNumber = Integer.parseInt(splitSegment[8]);
        segment.startByteNumber = Integer.parseInt(splitSegment[9]);
        segment.endByteNumber = Integer.parseInt(splitSegment[10]);
        segment.listSize = Integer.parseInt(splitSegment[11]);
        segment.flag = Integer.parseInt(splitSegment[12]);
        segment.message = splitSegment[13];
        segment.comment = splitSegment[14];

        return segment;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public String getDestinationIp() {
        return destinationIp;
    }

    public int getSourcePort() {
        return sourcePort;
    }

    public int getDestinationPort() {
        return destinationPort;
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

    public int getFlag() {
        return flag;
    }

    public String getMessage() {
        return message;
    }

    public String getComment() {
        return comment;
    }

    public String packSegment() {
        return sourceIp + SPLITS_CHAR +
                destinationIp + SPLITS_CHAR +
                sourcePort + SPLITS_CHAR +
                destinationPort + SPLITS_CHAR +
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
                "sourceIp='" + sourceIp + '\'' +
                ", destinationIp='" + destinationIp + '\'' +
                ", sourcePort=" + sourcePort +
                ", destinationPort=" + destinationPort +
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

        Builder setSourceIp(String sourceIp) {
            segment.sourceIp = sourceIp;
            return this;
        }

        Builder setDestinationIp(String destinationIp) {
            segment.destinationIp = destinationIp;
            return this;
        }

        Builder setSourcePort(int sourcePort) {
            segment.sourcePort = sourcePort;
            return this;
        }

        Builder setDestinationPort(int destinationPort) {
            segment.destinationPort = destinationPort;
            return this;
        }

        Builder setCommand(String command) {
            segment.command = command;
            return this;
        }

        Builder setFileName(String fileName) {
            segment.fileName = fileName;
            return this;
        }

        Builder setFileSize(int fileSize) {
            segment.fileSize = fileSize;
            return this;
        }

        Builder setMd5Sum(String md5Sum) {
            segment.md5Sum = md5Sum;
            return this;
        }

        Builder setSequenceNumber(int sequenceNumber) {
            segment.sequenceNumber = sequenceNumber;
            return this;
        }

        Builder setStartByteNumber(int startByteNumber) {
            segment.startByteNumber = startByteNumber;
            return this;
        }

        Builder setEndByteNumber(int endByteNumber) {
            segment.endByteNumber = endByteNumber;
            return this;
        }

        Builder setListSize(int listSize) {
            segment.listSize = listSize;
            return this;
        }

        Builder setFlag(int flag) {
            segment.flag = flag;
            return this;
        }

        Builder setMessage(String message) {
            segment.message = message;
            return this;
        }

        Builder setComment(String comment) {
            segment.comment = comment;
            return this;
        }

        Segment build() {
            return segment;
        }
    }
}
