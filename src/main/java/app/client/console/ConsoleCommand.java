package app.client.console;

public enum ConsoleCommand {

    CONNECT, // TODO BACKLOG Add manual connection forcing in multiHost version (required after connection completion)
    //           --> Then adding checking client connection with server and recommend to connect when client perform
    //           close earlier (is disconnected)
    FILE_LIST,
    PULL,
    PUSH,
    MULTIPLE_PULL, // only multihost
    CLOSE, // TODO BACKLOG implement fire that command after close java program
    EMPTY_COMMAND,
    UNSUPPORTED_COMMAND
}
