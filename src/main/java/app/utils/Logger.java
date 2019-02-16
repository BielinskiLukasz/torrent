package app.utils;

public class Logger {

    private static final boolean SERVER_LOG_DISPLAY = true;
    private static final boolean CLIENT_LOG_DISPLAY = true;
    private static final boolean CONSOLE_LOG_DISPLAY = true;
    private static final boolean DEBUG_LOG_DISPLAY = true;

    public static void serverLog(String message) {
        if (SERVER_LOG_DISPLAY)
            System.out.println(message);
        else {
            System.out.print("");
        }
    }

    public static void clientLog(String message) {
        if (CLIENT_LOG_DISPLAY)
            System.out.println(message);
        else {
            System.out.print("");
        }
    }

    public static void consoleLog(String message) {
        if (CONSOLE_LOG_DISPLAY)
            System.out.println(message);
        else {
            System.out.print("");
        }
    }

    public static void serverDebugLog(String message) {
        if (DEBUG_LOG_DISPLAY)
            System.out.println("Server: " + message);
        else {
            System.out.print("");
        }
    }

    public static void clientDebugLog(String message) {
        if (DEBUG_LOG_DISPLAY)
            System.out.println("Client: " + message);
        else {
            System.out.print("");
        }
    }

    public static void consoleDebugLog(String message) {
        if (DEBUG_LOG_DISPLAY)
            System.out.println("Console: " + message);
        else {
            System.out.print("");
        }
    }

    public static void utilsDebugLog(String message) {
        if (DEBUG_LOG_DISPLAY)
            System.out.println("Utils: " + message);
        else {
            System.out.print("");
        }
    }

    static void exceptionDebugLog(Exception e) {
        if (DEBUG_LOG_DISPLAY) {
            System.out.println("Stack Trace:");
            e.printStackTrace();
        } else {
            System.out.print("");
        }
    }
}
