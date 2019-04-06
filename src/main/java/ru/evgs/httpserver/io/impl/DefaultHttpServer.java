package ru.evgs.httpserver.io.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.evgs.httpserver.io.HttpServer;
import ru.evgs.httpserver.io.config.HttpServerConfig;
import ru.evgs.httpserver.io.exception.HttpServerException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
// the actual server itself managing connections and threads
public class DefaultHttpServer implements HttpServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultHttpServer.class);
    private final HttpServerConfig httpServerConfig; // server configuration
    private final ServerSocket serverSocket; // socket for each user connection
    private final ExecutorService executorService; // thread pool
    private final Thread mainServerThread; // main thread of server
    private volatile boolean serverStopped; // flag for stopping server
    private volatile boolean stopRequest; // flag for stopping task in thread

    DefaultHttpServer(HttpServerConfig httpServerConfig) {
        super();
        this.httpServerConfig = httpServerConfig;
        this.executorService = createExecutorService();
        this.mainServerThread = createMainServerThread(createServerRunnable());
        this.serverSocket = createServerSocket();
        this.serverStopped = false;
    }

    ServerSocket createServerSocket(){
        try {
            // creating socket on port
            ServerSocket serverSocket = new ServerSocket(httpServerConfig.getServerInfo().getPort());
            // for recreating socket on port while TCP Timeout
            serverSocket.setReuseAddress(true);
            return serverSocket;
        } catch (IOException e) {
            throw new HttpServerException("Can't create server socket with port=" + httpServerConfig.getServerInfo().getPort(), e);
        }
    }

    ExecutorService createExecutorService() {
        ThreadFactory threadFactory = httpServerConfig.getWorkerThreadFactory(); // getting thread factory configuration
        int threadCount = httpServerConfig.getServerInfo().getThreadCount(); // thread count
        if(threadCount > 0) {
            // creating fixed count of threads
            return Executors.newFixedThreadPool(threadCount, threadFactory);
        } else {
            // create threads as needed
            return Executors.newCachedThreadPool(threadFactory);
        }
    }
    // create a main thread that will act as a connection dispatcher
    Thread createMainServerThread(Runnable r) {
        Thread th = new Thread(r, "Main Server Thread");
        th.setPriority(Thread.MAX_PRIORITY);
        th.setDaemon(false);
        return th;
    }
    // runnable task for connection scheduling
    Runnable createServerRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                while (!mainServerThread.isInterrupted()) {
                    try {
                        // try to get connection from client socket
                        // blocking method
                        Socket clientSocket = serverSocket.accept();
                        executorService.submit(httpServerConfig.buildNewHttpClientSocketHandler(clientSocket));
                    } catch (IOException e) {
                        if (!serverSocket.isClosed()) {
                            LOGGER.error("Can't accept client socket: " + e.getMessage(), e);
                        }
                        destroyHttpServer();
                        break;
                    }
                }
                if(stopRequest){
                    System.exit(0);
                }
            }
        };
    }

    @Override
    public void start() {
        // check that the main thread was created now
        if (mainServerThread.getState() != Thread.State.NEW) {
            throw new HttpServerException("Current web server already started or stopped! Please create a new http server instance");
        }
        Runtime.getRuntime().addShutdownHook(getShutdownHook());
        mainServerThread.start();
        LOGGER.info("Server started: " + httpServerConfig.getServerInfo());
    }

    @Override
    public void stop() {
        LOGGER.info("Detected stop cmd");
        stopRequest = true;
        mainServerThread.interrupt();
        try {
            serverSocket.close();
        } catch (IOException e) {
            LOGGER.warn("Error during close server socket: " + e.getMessage(), e);
        }
    }
    // cleaning up resources due to stopping the virtual machine
    Thread getShutdownHook() {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                if (!serverStopped) {
                    destroyHttpServer();
                }
            }
        }, "ShutdownHook");
    }
    // free up server resources
    void destroyHttpServer() {
        try {
            httpServerConfig.close();
        } catch (Exception e) {
            LOGGER.error("Close httpServerConfig failed: " + e.getMessage(), e);
        }
        // inform threads that it is not necessary to work more
        executorService.shutdownNow();
        LOGGER.info("Server stopped");
        serverStopped = true;
    }
}
