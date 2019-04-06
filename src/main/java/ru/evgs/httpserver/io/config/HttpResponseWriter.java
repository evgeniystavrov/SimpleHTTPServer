package ru.evgs.httpserver.io.config;

import java.io.IOException;
import java.io.OutputStream;

// response record interface
public interface HttpResponseWriter {

    void writeHttpResponse(OutputStream out, ReadableHttpResponse response) throws IOException;
}
