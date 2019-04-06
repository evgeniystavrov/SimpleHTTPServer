package ru.evgs.httpserver.io;

// class that stores server information
public class ServerInfo {
    // server name
    private final String name;
    // server port
    private final int port;
    // number of server threads
    private final int threadCount;

    public ServerInfo(String name, int port, int threadCount) {
        super();
        this.name = name;
        this.port = port;
        this.threadCount = threadCount;
    }

    public String getName() {
        return name;
    }

    public int getPort() {
        return port;
    }

    public int getThreadCount() {
        return threadCount;
    }

    @Override
    public String toString() {
        return String.format("ServerInfo [name = %s, port = %s, threadCount = %s]", name, port, threadCount == 0 ? "UNLIMITED" : threadCount);
    }
}
