package ru.evgs.httpserver.io.config;

// http response forming interface
public interface HttpResponseBuilder {

    ReadableHttpResponse buildNewHttpResponse();

    void prepareHttpResponse(ReadableHttpResponse response, boolean clearBody);
}
