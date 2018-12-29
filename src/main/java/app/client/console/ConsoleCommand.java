package app.client.console;

public enum ConsoleCommand {

    CONNECT, // TODO Add manual connection forcing in multiHost version (required after connection completion)
    //           --> Then adding checking client connection with server and recommend to connect when client perform
    //           close earlier (is disconnected)
    FILE_LIST,
    PULL,
    PUSH,
    REPULL,
    REPUSH,
    MULTIPLE_PULL,
    CLOSE, // Not used in h2h version
    EMPTY_COMMAND,
    UNSUPPORTED_COMMAND
}