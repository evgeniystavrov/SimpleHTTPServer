package ru.evgs.httpserver.io.impl;

import ru.evgs.httpserver.io.HandlerConfig;
import ru.evgs.httpserver.io.HttpServer;
import ru.evgs.httpserver.io.config.HttpServerConfig;

import java.util.Properties;

// factory to create a http server object
public class HttpServerFactory {
    protected HttpServerFactory(){

    }

    public static HttpServerFactory create() {
        return new HttpServerFactory();
    }

    public HttpServer createHttpServer(HandlerConfig handlerConfig, Properties overrideServerProperties) {
        // creating configuration
        HttpServerConfig httpServerConfig = new DefaultHttpServerConfig(handlerConfig, overrideServerProperties);
        return new DefaultHttpServer(httpServerConfig);
    }
}
