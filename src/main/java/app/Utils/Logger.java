package app.Utils;

public class Logger {

    private static final boolean SERVER_LOG_DISPLAY = true;
    private static final boolean CLIENT_LOG_DISPLAY = true;
    private static final boolean APP_LOG_DISPLAY = true;
    private static final boolean DEBUG_LOG_DISPLAY = false;

    public static void serverLog(String message) {
        if (SERVER_LOG_DISPLAY)
            System.out.println(message);
    }

    public static void clientLog(String message) {
        if (CLIENT_LOG_DISPLAY)
            System.out.println(message);
    }

    public static void appLog(String message) {
        if (APP_LOG_DISPLAY)
            System.out.println(message);
    }

    public static void serverDebugLog(String message) {
        if (DEBUG_LOG_DISPLAY)
            System.out.println("Server: " + message);
    }

    public static void clientDebugLog(String message) {
        if (DEBUG_LOG_DISPLAY)
            System.out.println("Client: " + message);
    }

    public static void appDebugLog(String message) {
        if (DEBUG_LOG_DISPLAY)
            System.out.println("App: " + message);
    }
}
