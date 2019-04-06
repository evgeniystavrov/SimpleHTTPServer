package ru.evgs.httpserver.io.impl;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.evgs.httpserver.io.Constants;
import ru.evgs.httpserver.io.HttpRequest;
import ru.evgs.httpserver.io.HttpResponse;
import ru.evgs.httpserver.io.HttpServerContext;
import ru.evgs.httpserver.io.config.HttpClientSocketHandler;
import ru.evgs.httpserver.io.config.HttpServerConfig;
import ru.evgs.httpserver.io.config.ReadableHttpResponse;
import ru.evgs.httpserver.io.exception.AbstractRequestParseFailedException;
import ru.evgs.httpserver.io.exception.HttpServerException;
import ru.evgs.httpserver.io.exception.MethodNotAllowedException;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
// class responsible for processing the request in a separate thread
// short-lived, created for each user connection
public class DefaultHttpClientSocketHandler implements HttpClientSocketHandler {
    private static final Logger ACCESS_LOGGER = LoggerFactory.getLogger("ACCESS_LOG");
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultHttpClientSocketHandler.class);
    private final Socket clientSocket; // socket connection
    private final String remoteAddress; // current connection remote address
    private final HttpServerConfig httpServerConfig; // current server config

    DefaultHttpClientSocketHandler(Socket clientSocket, HttpServerConfig httpServerConfig) {
        super();
        this.clientSocket = clientSocket;
        this.remoteAddress = clientSocket.getRemoteSocketAddress().toString();
        this.httpServerConfig = httpServerConfig;
    }

    @Override
    public void run() {
        try {
            execute();
        } catch (Exception e) {
            LOGGER.error("Client request failed: " + e.getMessage(), e);
        }
    }

    protected void execute() throws Exception {
        try (Socket s = clientSocket) {
            s.setKeepAlive(false);
            try (InputStream in = s.getInputStream(); OutputStream out = s.getOutputStream()) {
                processRequest(remoteAddress, in, out);
            }
        }
    }

    protected void processRequest(String remoteAddress, InputStream in, OutputStream out) throws IOException {
        // creating new response object
        ReadableHttpResponse response = httpServerConfig.getHttpResponseBuilder().buildNewHttpResponse();
        // set starting line as null
        String startingLine = null;
        try {
            // parse request
            HttpRequest request = httpServerConfig.getHttpRequestParser().parseHttpRequest(in, remoteAddress);
            // getting starting line
            startingLine = request.getStartingLine();
            processRequest(request, response);
        } catch (AbstractRequestParseFailedException e) {
            startingLine = e.getStartingLine();
            handleException(e, response);
        } catch (EOFException e) {
            LOGGER.warn("Client socket closed connection");
            return;
        }
        httpServerConfig.getHttpResponseBuilder().prepareHttpResponse(response, startingLine.startsWith(Constants.HEAD));
        ACCESS_LOGGER.info("Request: {} - \"{}\", Response: {} ({} bytes)", remoteAddress, startingLine, response.getStatus(), response.getBodyLength());
        httpServerConfig.getHttpResponseWriter().writeHttpResponse(out, response);
    }

    protected void processRequest(HttpRequest request, HttpResponse response) {
        // getting server current context
        HttpServerContext context = httpServerConfig.getHttpServerContext();
        try {
            // handle request
            httpServerConfig.getHttpRequestDispatcher().handle(context, request, response);
        } catch (Exception e) {
            // see what the error is and handle it with the appropriate handler
            handleException(e, response);
        }
    }
    // exception handling
    protected void handleException(Exception ex, HttpResponse response) {
        LOGGER.error("Exception during request: " + ex.getMessage(), ex);
        if (ex instanceof HttpServerException) {
            HttpServerException e = (HttpServerException) ex;
            response.setStatus(e.getStatusCode());
            if (e instanceof MethodNotAllowedException) {
                response.setHeader("Allow", StringUtils.join(Constants.ALLOWED_METHODS, ", "));
            }
        } else {
            response.setStatus(500);
        }
    }
}
