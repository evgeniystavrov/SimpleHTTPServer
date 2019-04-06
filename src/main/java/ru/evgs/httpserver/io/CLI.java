package ru.evgs.httpserver.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.evgs.httpserver.io.handler.HelloWorldHttpHandler;
import ru.evgs.httpserver.io.handler.ServerInfoHttpHandler;
import ru.evgs.httpserver.io.handler.TestJDBCHandler;
import ru.evgs.httpserver.io.impl.HttpServerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

// command line interface to start and stop the server
public class CLI {
    // create logger and command set
    private static final Logger LOGGER = LoggerFactory.getLogger(CLI.class);
    private static final List<String> QUIT_CMDS = Collections.unmodifiableList(Arrays.asList(new String[] { "q", "quit", "exit" }));

    public static void main(String[] args) {
        // create a stream with the server object and start it, waiting for the command to stop
        Thread.currentThread().setName("CLI-main thread");
        try {
            HttpServerFactory httpServerFactory = HttpServerFactory.create();
            HttpServer httpServer = httpServerFactory.createHttpServer(getHandlerConfig(), null);
            httpServer.start();
            waitForStopCommand(httpServer);
        } catch (Exception e) {
            // we catch an error, if something goes wrong at startup, let's see what kind of error
            LOGGER.error("Can't execute cmd: " + e.getMessage(), e);
        }
    }
    // the method that processes the input stream, waits for the stop command from the corresponding set
    private static void waitForStopCommand(HttpServer httpServer) {
        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8.name())) {
            while (true) {
                String cmd = scanner.nextLine();
                if (QUIT_CMDS.contains(cmd.toLowerCase())) {
                    httpServer.stop();
                    break;
                } else {
                    LOGGER.error("Undefined command: " + cmd + "! To shutdown server please type: q");
                }
            }
        }
    }
    // the method returns the corresponding page handler
    private static HandlerConfig getHandlerConfig() {
        return new HandlerConfig()
                .addHandler("/info", new ServerInfoHttpHandler())
                .addHandler("/jdbc", new TestJDBCHandler())
                .addHandler("/hello", new HelloWorldHttpHandler());
    }
}
