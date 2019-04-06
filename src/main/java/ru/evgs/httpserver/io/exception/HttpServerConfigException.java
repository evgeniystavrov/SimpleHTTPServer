package ru.evgs.httpserver.io.exception;

// exception to handle server configuration errors
public class HttpServerConfigException extends HttpServerException {
    private static final long serialVersionUID = -6231238429653448404L;

    public HttpServerConfigException(String message) {
        super(message);
    }

    public HttpServerConfigException(Throwable cause) {
        super(cause);
    }

    public HttpServerConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
