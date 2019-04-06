package ru.evgs.httpserver.io;

import java.io.InputStream;
import java.io.Reader;

// interface that forms the http response
public interface HttpResponse {
    // forms status
    void setStatus(int status);
    // forms header
    void setHeader(String name, Object value);
    // methods that forms body in different ways
    void setBody(String content);

    void setBody(InputStream in);

    void setBody(Reader reader);
}
