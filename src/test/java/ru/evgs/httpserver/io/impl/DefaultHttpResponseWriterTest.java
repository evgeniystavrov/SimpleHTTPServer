package ru.evgs.httpserver.io.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.WriterOutputStream;
import org.junit.Before;
import org.junit.Test;
import ru.evgs.httpserver.io.config.HttpResponseWriter;
import ru.evgs.httpserver.io.config.HttpServerConfig;
import ru.evgs.httpserver.io.config.ReadableHttpResponse;

public class DefaultHttpResponseWriterTest {
    private HttpResponseWriter writer;
    private HttpServerConfig httpServerConfig;
    private ReadableHttpResponse response;

    @Before
    public void before(){
        httpServerConfig = mock(HttpServerConfig.class);
        writer = new DefaultHttpResponseWriter(httpServerConfig);
        when(httpServerConfig.getStatusMessage(200)).thenReturn("OK");
        response = new DefaultReadableHttpResponse();
        response.setHeader("Header1", "value1");
        response.setHeader("Header2", "value2");
    }

    private InputStream getClassPathResourceStream(String resourceName) {
        return getClass().getClassLoader().getResourceAsStream(resourceName);
    }

    @Test
    public void testWriteResponseWithBody() throws IOException{
        response.setBody("HTTP message body");

        StringWriter sw = new StringWriter();
        writer.writeHttpResponse(new WriterOutputStream(sw, StandardCharsets.UTF_8), response);

        try(InputStream in = getClassPathResourceStream("http-response-with-body.txt")) {
            String expected = IOUtils.toString(in, StandardCharsets.UTF_8);
            String actual = sw.toString();
            assertEquals(expected, actual);
        }
    }

    @Test
    public void testWriteResponseWithoutBody() throws IOException{
        StringWriter sw = new StringWriter();
        writer.writeHttpResponse(new WriterOutputStream(sw, StandardCharsets.UTF_8), response);

        try(InputStream in = getClassPathResourceStream("http-response-without-body.txt")) {
            String expected = IOUtils.toString(in, StandardCharsets.UTF_8);
            String actual = sw.toString();
            assertEquals(expected, actual);
        }
    }
}
