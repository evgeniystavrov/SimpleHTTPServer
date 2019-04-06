package ru.evgs.httpserver.io;

import java.util.Map;

// interface that providing information from the http request
public interface HttpRequest {
    // method that retrieves the starting line
    String getStartingLine();
    // method that retrieves the http method
    String getMethod();
    // method that retrieves the uri
    String getUri();
    // method that retrieves the version of http protocol
    String getHttpVersion();
    // returns the connection address
    String getRemoteAddress();
    // method that retrieves headers
    Map<String, String> getHeaders();
    // method that retrieves parameters
    Map<String, String> getParameters();
}
