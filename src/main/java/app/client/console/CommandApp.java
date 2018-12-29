package app.client.console;

public enum CommandApp {

    CONNECT, // TODO Add manual connection forcing (required after connection completion)
    //           --> Then adding checking client connection with server and recommend to connect when client perform
    //           close earlier (is disconnected)
    FILE_LIST,
    PULL,
    PUSH,
    REPULL,
    REPUSH,
    MULTIPLE_PULL,
    CLOSE,
    EMPTY_COMMAND,
    UNSUPPORTED_COMMAND
}
