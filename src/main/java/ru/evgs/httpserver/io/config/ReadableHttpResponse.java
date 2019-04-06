package ru.evgs.httpserver.io.config;

import ru.evgs.httpserver.io.HttpResponse;

import java.util.Map;

// interface that describes how to read data
public interface ReadableHttpResponse extends HttpResponse {

    int getStatus();

    Map<String, String> getHeaders();

    byte[] getBody();

    boolean isBodyEmpty();

    int getBodyLength();
}
