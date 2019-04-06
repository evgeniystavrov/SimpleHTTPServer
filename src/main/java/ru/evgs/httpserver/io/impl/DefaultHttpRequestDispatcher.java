package ru.evgs.httpserver.io.impl;

import ru.evgs.httpserver.io.HttpHandler;
import ru.evgs.httpserver.io.HttpRequest;
import ru.evgs.httpserver.io.HttpResponse;
import ru.evgs.httpserver.io.HttpServerContext;
import ru.evgs.httpserver.io.config.HttpRequestDispatcher;
import ru.evgs.httpserver.io.exception.HttpServerException;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

class DefaultHttpRequestDispatcher implements HttpRequestDispatcher {
    private final HttpHandler defaultHttpHandler;
    private final Map<String, HttpHandler> httpHandlers;

    DefaultHttpRequestDispatcher(HttpHandler defaultHttpHandler, Map<String, HttpHandler> httpHandlers) {
        super();
        Objects.requireNonNull(defaultHttpHandler, "Default handler should be not null");
        Objects.requireNonNull(httpHandlers, "httpHandlers should be not null");
        this.defaultHttpHandler = defaultHttpHandler;
        this.httpHandlers = httpHandlers;
    }

    @Override
    public void handle(HttpServerContext context, HttpRequest request, HttpResponse response) throws IOException {
        try {
            // getting handler by request
            HttpHandler handler = getHttpHandler(request);
            handler.handle(context, request, response);
        } catch (RuntimeException e) {
            if (e instanceof HttpServerException) {
                throw e;
            } else {
                throw new HttpServerException("Handle request: " + request.getUri() + " failed: " + e.getMessage(), e);
            }
        }
    }

    HttpHandler getHttpHandler(HttpRequest request) {
        // getting handler from map
        HttpHandler handler = httpHandlers.get(request.getUri());
        // if we have no handler in map we use a default handler
        if (handler == null) {
            handler = defaultHttpHandler;
        }
        return handler;
    }
}
