package ru.evgs.httpserver.io;

import ru.evgs.httpserver.io.exception.HttpServerConfigException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
// class to configure handlers
public final class HandlerConfig {
    private final Map<String, HttpHandler> httpHandlers = new HashMap<>();
    // forming map with handlers
    public HandlerConfig addHandler(String url, HttpHandler httpHandler) {
        Objects.requireNonNull(url);
        Objects.requireNonNull(httpHandler);
        HttpHandler prevHttpHandler = httpHandlers.get(url);
        if (prevHttpHandler != null) {
            throw new HttpServerConfigException("Http handler already exists for url=" + url +
                    ". Http handler class: " + prevHttpHandler.getClass().getName());
        }
        httpHandlers.put(url, httpHandler);
        return this;
    }
    // returning unmodifiable
    public Map<String, HttpHandler> toMap() {
        return Collections.unmodifiableMap(httpHandlers);
    }
}
