package ru.evgs.httpserver.io.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;

import java.util.concurrent.ThreadFactory;

import org.junit.Before;
import org.junit.Test;

public class DefaultThreadFactoryTest {
    private ThreadFactory threadFactory;

    @Before
    public void before(){
        threadFactory = new DefaultThreadFactory();
    }

    @Test
    public void testNewThread(){
        Runnable r = mock(Runnable.class);
        Thread th = threadFactory.newThread(r);
        assertEquals(8, th.getPriority());
        assertFalse(th.isDaemon());
        assertEquals("executor-thread-1", th.getName());
    }
}
