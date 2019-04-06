package ru.evgs.httpserver.io.config;

import ru.evgs.httpserver.io.HttpRequest;
import ru.evgs.httpserver.io.exception.HttpServerException;

import java.io.IOException;
import java.io.InputStream;

// interface responsible for parsing the request
public interface HttpRequestParser {

    HttpRequest parseHttpRequest(InputStream inputStream, String remoteAddress) throws IOException, HttpServerException;
}
