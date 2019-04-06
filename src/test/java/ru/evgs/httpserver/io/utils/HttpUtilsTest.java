package ru.evgs.httpserver.io.utils;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.input.ReaderInputStream;
import org.hamcrest.core.IsEqual;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class HttpUtilsTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testNormilizeHeaderName(){
        assertEquals("Content-Type", HttpUtils.normilizeHeaderName("Content-Type"));
        assertEquals("Content-Type", HttpUtils.normilizeHeaderName("content-type"));
        assertEquals("Content-Type", HttpUtils.normilizeHeaderName("CONTENT-TYPE"));
        assertEquals("Content-Type", HttpUtils.normilizeHeaderName("CONTENT-type"));
        assertEquals("Content-Type", HttpUtils.normilizeHeaderName("CoNtEnT-tYpE"));

        assertEquals("Expires", HttpUtils.normilizeHeaderName("Expires"));
        assertEquals("Expires", HttpUtils.normilizeHeaderName("expires"));
        assertEquals("Expires", HttpUtils.normilizeHeaderName("EXPIRES"));

        assertEquals("If-Modified-Since", HttpUtils.normilizeHeaderName("If-Modified-Since"));
        assertEquals("If-Modified-Since", HttpUtils.normilizeHeaderName("if-modified-since"));
        assertEquals("If-Modified-Since", HttpUtils.normilizeHeaderName("IF-MODIFIED-SINCE"));

        assertEquals("Test-", HttpUtils.normilizeHeaderName("Test-"));
        assertEquals("Test-", HttpUtils.normilizeHeaderName("TEST-"));
        assertEquals("Test-", HttpUtils.normilizeHeaderName("test-"));
    }

    @Test
    public void testEOFException() throws IOException{
        thrown.expect(EOFException.class);
        thrown.expectMessage(new IsEqual<String>("InputStream closed"));
        InputStream in = mock(InputStream.class);
        when(in.read()).thenReturn(-1);

        HttpUtils.readStartingLineAndHeaders(in);
    }

    @Test
    public void testReadStartingLineAndHeadersLargeRequest() throws IOException{
        StringBuilder largeRequest = new StringBuilder("GET /index.html HTTP/1.1\r\n");
        for(int i=0;i<1000;i++) {
            largeRequest.append("Header"+i+": value"+i+"\r\n");
        }
        largeRequest.append("\r\n");

        String startingLineAndHeaders = HttpUtils.readStartingLineAndHeaders(new ReaderInputStream(new StringReader(largeRequest.toString()), StandardCharsets.UTF_8));
        // -4 because HttpUtils.ByteArray.toArray() extract 4 bytes for last empty line
        assertEquals(largeRequest.length() - 4, startingLineAndHeaders.length());
    }

    @Test
    public void testGetContentLengthValue() throws IOException{
        int result = HttpUtils.getContentLengthValue("content-length: 12", 0);
        assertEquals(12, result);
    }
}
