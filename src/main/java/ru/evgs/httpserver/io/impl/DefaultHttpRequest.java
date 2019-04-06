package ru.evgs.httpserver.io.impl;

import ru.evgs.httpserver.io.HttpRequest;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

// class retrieves information from the http request
// class can be used only within the package
class DefaultHttpRequest implements HttpRequest {
    // fields storing request data as constants
    private final String method;
    private final String uri;
    private final String httpVersion;
    private final String remoteAddress;
    private final Map<String, String> headers;
    private final Map<String, String> parameters;

    DefaultHttpRequest(String method, String uri, String httpVersion, String remoteAddress, Map<String, String> headers, Map<String, String> parameters) {
        super();
        this.method = method;
        this.uri = uri;
        this.httpVersion = httpVersion;
        this.remoteAddress = remoteAddress;
        // wrapping the collection for immutability
        this.headers = Collections.unmodifiableMap(headers);
        this.parameters = Collections.unmodifiableMap(parameters);
    }

    @Override
    public String getStartingLine() {
        return String.format("%s %s %s",getMethod(), getUri(), getHttpVersion());
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public String getHttpVersion() {
        return httpVersion;
    }

    @Override
    public String getRemoteAddress() {
        return remoteAddress;
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public Map<String, String> getParameters() {
        return parameters;
    }
}
