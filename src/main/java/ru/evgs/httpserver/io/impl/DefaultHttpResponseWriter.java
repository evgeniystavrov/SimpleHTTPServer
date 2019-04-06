package ru.evgs.httpserver.io.impl;

import ru.evgs.httpserver.io.Constants;
import ru.evgs.httpserver.io.config.HttpResponseWriter;
import ru.evgs.httpserver.io.config.HttpServerConfig;
import ru.evgs.httpserver.io.config.ReadableHttpResponse;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
// class for writing the response
class DefaultHttpResponseWriter extends AbstractHttpConfigurableComponent implements HttpResponseWriter {
    DefaultHttpResponseWriter(HttpServerConfig httpServerConfig) {
        super(httpServerConfig);
    }
    // main writing method
    @Override
    public void writeHttpResponse(OutputStream out, ReadableHttpResponse response) throws IOException {
        // opening buffering stream
        PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8)));
        // adding starting line and headers
        addStartingLine(writer, response);
        addHeaders(writer, response);
        writer.println();
        writer.flush();
        // adding body
        addMessageBody(out, response);
    }

    protected void addStartingLine(PrintWriter out, ReadableHttpResponse response) {
        // we get http version
        String httpVersion = Constants.HTTP_VERSION;
        // we get status code
        int statusCode = response.getStatus();
        // we get message for status code
        String statusMessage = httpServerConfig.getStatusMessage(statusCode);
        //HTTP/1.1 200 Ok
        out.println(String.format("%s %s %s", httpVersion, statusCode, statusMessage));
    }

    protected void addHeaders(PrintWriter out, ReadableHttpResponse response) {
        for(Map.Entry<String, String> entry : response.getHeaders().entrySet()) {
            // Content-Type: text/plain
            out.println(String.format("%s: %s", entry.getKey(), entry.getValue()));
        }
    }
    // forming body
    protected void addMessageBody(OutputStream out, ReadableHttpResponse response) throws IOException {
        if(!response.isBodyEmpty()) {
            out.write(response.getBody());
            out.flush();
        }
    }
}
