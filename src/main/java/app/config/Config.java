package app.config;

public class Config {

    public static final String BASIC_PATH = "TORrent_";
    public static final String HOST_IP = "127.0.0.1";
    public static final int PORT_NR = 10000;

    public static final int BUFFER_SIZE_IN_BYTES = 4096;
    public static final int PACKET_MIN_SIZE_IN_KB = 64;
    public static final int PACKET_MAX_SIZE_IN_KB = 1024;

    public static final int MAX_NUMBER_OF_PARAMETERS = 3;
    private static final char EDITABLE_SPLITS_CHAR = '*';
    public static final String SPLITS_CHAR = String.valueOf(EDITABLE_SPLITS_CHAR);
    private static final char EDITABLE_FILE_INFO_SPLITS_CHAR = '|';
    public static final String FILE_INFO_SPLITS_CHAR = String.valueOf(EDITABLE_FILE_INFO_SPLITS_CHAR);

    public static final int WAITING_TIME_SEC = 10; // TODO increase
    public static final int MILLISECONDS_OF_CONNECTION_LISTENER_WAITING = 1000;
    public static final int MAX_NUMBER_OF_RECONNECT = 5; // TODO BACKLOG implement usage

    public static final int INT_SV = -1; // used to creating first client in host2host version
}