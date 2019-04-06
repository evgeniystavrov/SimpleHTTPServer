package ru.evgs.httpserver.io.impl;

import java.util.concurrent.ThreadFactory;
// class for creating server threads
class DefaultThreadFactory implements ThreadFactory {
    private String name;
    private int count;

    DefaultThreadFactory() {
        super();
        count = 1;
        name = "executor-thread-";
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread th = new Thread(r, name + (count++));
        th.setDaemon(false);
        th.setPriority(8); // middle priority
        return th;
    }
}
