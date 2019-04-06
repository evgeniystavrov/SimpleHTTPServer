package ru.evgs.httpserver.io.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import ru.evgs.httpserver.io.HttpServer;

public class HttpServerFactoryTest {
    private HttpServerFactory httpServerFactory;

    @Before
    public void before(){
        httpServerFactory = HttpServerFactory.create();
    }

    @Test
    public void testCreate(){
        assertEquals(HttpServerFactory.class, httpServerFactory.getClass());
    }

    @Test
    public void testCreateHttpServer(){
        HttpServer server = httpServerFactory.createHttpServer(null, null);
        assertEquals(DefaultHttpServer.class, server.getClass());
    }
}
