package app.utils;

public class ExceptionHandler {

    public static void handle(Exception e) {
        System.out.println(e.getMessage());
        Logger.exceptionDebugLog(e);
    }
}
