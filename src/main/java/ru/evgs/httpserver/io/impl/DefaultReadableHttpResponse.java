package ru.evgs.httpserver.io.impl;

import org.apache.commons.io.IOUtils;
import ru.evgs.httpserver.io.config.ReadableHttpResponse;
import ru.evgs.httpserver.io.exception.HttpServerException;
import ru.evgs.httpserver.io.utils.HttpUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

// class that implements the functional formation of the correct http response
// class can be used only within the package
class DefaultReadableHttpResponse implements ReadableHttpResponse {
    // headerы cannot be changed
    private final Map<String, String> headers;
    // body and status are formed
    private byte[] body;
    private int status;
    // the only constructor initializing fields
    protected DefaultReadableHttpResponse() {
        this.status = 200;
        this.headers = new LinkedHashMap<>();
        this.body = new byte[0];
    }

    @Override
    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public void setHeader(String name, Object value) {
        // check that the name and value are not null
        Objects.requireNonNull(name, "Name can't be null");
        Objects.requireNonNull(value, "Value can't be null");
        // use the utility class to normalize the name
        name = HttpUtils.normilizeHeaderName(name);
        if(value instanceof Date) {
            // if the value is a date, then format it according to the protocol specification
            headers.put(name, new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z").format(value));
        } else if(value instanceof FileTime) {
            // same, if value is the value of a file's time stamp attribute
            headers.put(name, new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z").format(new Date(((FileTime) value).toMillis())));
        } else {
            // if not all of the above, then convert the value to a string and write to the collection
            headers.put(name, String.valueOf(value));
        }
    }
    // body setting by string content
    @Override
    public void setBody(String content) {
        // check that is not null
        Objects.requireNonNull(content, "Content can't be null");
        this.body = content.getBytes(StandardCharsets.UTF_8);
    }
    // body formation by the input byte stream
    @Override
    public void setBody(InputStream in) {
        try {
            // again that is not null
            Objects.requireNonNull(in, "InputStream can't be null");
            this.body = IOUtils.toByteArray(in);
        } catch (IOException e) {
            // catching an I / O error
            throw new HttpServerException("Can't set http response body from inputstream: " + e.getMessage(), e);
        }
    }
    // body formation by the input char stream
    @Override
    public void setBody(Reader reader) {
        try {
            // again that is not null
            Objects.requireNonNull(reader, "Reader can't be null");
            this.body = IOUtils.toByteArray(reader, StandardCharsets.UTF_8);
        } catch (IOException e) {
            // and again catching an I / O error
            throw new HttpServerException("Can't set http response body from reader: " + e.getMessage(), e);
        }
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public byte[] getBody() {
        return body;
    }
    // check is it empty
    @Override
    public boolean isBodyEmpty() {
        return getBodyLength() == 0;
    }
    // returns the length of the body array
    @Override
    public int getBodyLength() {
        return body.length;
    }
}
