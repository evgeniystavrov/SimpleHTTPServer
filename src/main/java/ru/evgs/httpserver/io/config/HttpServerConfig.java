package ru.evgs.httpserver.io.config;

import ru.evgs.httpserver.io.HttpServerContext;
import ru.evgs.httpserver.io.ServerInfo;

import java.net.Socket;
import java.util.concurrent.ThreadFactory;

// an interface defining which objects should be created for the current connection thread
// AutoCloseable for using with try with resource
public interface HttpServerConfig extends AutoCloseable {

    ServerInfo getServerInfo();

    String getStatusMessage(int statusCode);

    HttpRequestParser getHttpRequestParser();

    HttpResponseBuilder getHttpResponseBuilder();

    HttpResponseWriter getHttpResponseWriter();

    HttpServerContext getHttpServerContext();

    HttpRequestDispatcher getHttpRequestDispatcher();

    ThreadFactory getWorkerThreadFactory();

    HttpClientSocketHandler buildNewHttpClientSocketHandler(Socket clientSocket);
}
