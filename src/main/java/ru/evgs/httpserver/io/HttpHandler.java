package ru.evgs.httpserver.io;

import java.io.IOException;

// interface defining the request handler
public interface HttpHandler {

    void handle(HttpServerContext context, HttpRequest request, HttpResponse response) throws IOException;
}
