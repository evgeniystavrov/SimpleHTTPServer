package ru.evgs.httpserver.io.exception;

import ru.evgs.httpserver.io.Constants;

// exception for unsupported method
public class MethodNotAllowedException extends AbstractRequestParseFailedException {
    private static final long serialVersionUID = 1226345593185916605L;

    public MethodNotAllowedException(String method, String startingLine) {
        super("Only " + Constants.ALLOWED_METHODS + " are supported. Current method is " + method, startingLine);
        setStatusCode(405);
    }
}
