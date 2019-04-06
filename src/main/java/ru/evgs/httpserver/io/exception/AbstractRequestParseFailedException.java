package ru.evgs.httpserver.io.exception;

// exception related to the inability to parse the current request
public abstract class AbstractRequestParseFailedException extends HttpServerException {
    private static final long serialVersionUID = -6160401450155043320L;
    private final String startingLine;

    public AbstractRequestParseFailedException(String message, String startingLine) {
        super(message);
        this.startingLine = startingLine;
    }

    public AbstractRequestParseFailedException(Throwable cause, String startingLine) {
        super(cause);
        this.startingLine = startingLine;
    }

    public AbstractRequestParseFailedException(String message, Throwable cause, String startingLine) {
        super(message, cause);
        this.startingLine = startingLine;
    }

    public String getStartingLine() {
        return startingLine;
    }
}
