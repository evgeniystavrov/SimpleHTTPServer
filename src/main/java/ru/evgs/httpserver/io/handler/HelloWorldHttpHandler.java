package ru.evgs.httpserver.io.handler;

import ru.evgs.httpserver.io.HttpHandler;
import ru.evgs.httpserver.io.HttpRequest;
import ru.evgs.httpserver.io.HttpResponse;
import ru.evgs.httpserver.io.HttpServerContext;

import java.io.IOException;

public class HelloWorldHttpHandler implements HttpHandler {

    @Override
    public void handle(HttpServerContext context, HttpRequest request, HttpResponse response) throws IOException {
        // setting body by phrase from Kernighan and Ritchie
        response.setBody("Hello world");
    }
}
