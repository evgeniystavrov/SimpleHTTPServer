package ru.evgs.httpserver.io.impl;

import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.evgs.httpserver.io.*;
import ru.evgs.httpserver.io.config.*;
import ru.evgs.httpserver.io.exception.HttpServerConfigException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ThreadFactory;
// contains the entire configuration of the current server
// creates and config all server elements
class DefaultHttpServerConfig implements HttpServerConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultHttpServerConfig.class);

    private final Properties serverProperties = new Properties(); // main properties of current server
    private final Properties statusesProperties = new Properties(); // supported statuses and messages to them
    private final Properties mimeTypesProperties = new Properties(); // supported mime-types
    private final Map<String, HttpHandler> httpHandlers; // page handler set
    private final BasicDataSource dataSource; // object for working with relational database
    private final Path rootPath; // root directory for accessing to files
    private final HttpServerContext httpServerContext; // server context object
    private final HttpRequestParser httpRequestParser; // request parser object
    private final HttpResponseWriter httpResponseWriter; // response writer object
    private final HttpResponseBuilder httpResponseBuilder; // response builder bject
    private final HttpRequestDispatcher httpRequestDispatcher; // dispatcher for requests
    private final HttpHandler defaultHttpHandler; // resource processing
    private final ThreadFactory workerThreadFactory; // thread-generating object
    private final HtmlTemplateManager htmlTemplateManager; // template processing
    private final ServerInfo serverInfo;
    private final List<String> staticExpiresExtensions; // static resources formats
    private final int staticExpiresDays; // static resource storage time for browser

    @SuppressWarnings("unchecked")
    DefaultHttpServerConfig(HandlerConfig handlerConfig, Properties overrideServerProperties) {
        super();
        loadAllProperties(overrideServerProperties);
        this.httpHandlers = handlerConfig != null ? handlerConfig.toMap() : (Map<String, HttpHandler>) Collections.EMPTY_MAP;
        this.rootPath = createRootPath();
        this.dataSource = createBasicDataSource();
        this.serverInfo = createServerInfo();
        this.staticExpiresDays = Integer.parseInt(this.serverProperties.getProperty("webapp.static.expires.days"));
        this.staticExpiresExtensions = Arrays.asList(this.serverProperties.getProperty("webapp.static.expires.extensions").split(","));

        // create default implementations
        this.httpServerContext = new DefaultHttpServerContext(this);
        this.httpRequestParser = new DefaultHttpRequestParser();
        this.httpResponseWriter = new DefaultHttpResponseWriter(this);
        this.httpResponseBuilder = new DefaultHttpResponseBuilder(this);
        this.defaultHttpHandler = new DefaultHttpHandler();
        this.httpRequestDispatcher = new DefaultHttpRequestDispatcher(defaultHttpHandler, this.httpHandlers);
        this.workerThreadFactory = new DefaultThreadFactory();
        this.htmlTemplateManager = new DefaultHtmlTemplateManager();
    }
    // loading specified properties files
    protected void loadAllProperties(Properties overrideServerProperties) {
        ClassLoader classLoader = DefaultHttpServerConfig.class.getClassLoader();
        loadProperties(this.statusesProperties, classLoader, "statuses.properties");
        loadProperties(this.mimeTypesProperties, classLoader, "mime-types.properties");
        loadProperties(this.serverProperties, classLoader, "server.properties");
        if (overrideServerProperties != null) {
            LOGGER.info("Overrides default server properties");
            this.serverProperties.putAll(overrideServerProperties);
        }
        logServerProperties();
    }
    // logging of current server properties
    protected void logServerProperties() {
        if (LOGGER.isDebugEnabled()) {
            StringBuilder res = new StringBuilder("Current server properties is:\n");
            for (Map.Entry<Object, Object> entry : this.serverProperties.entrySet()) {
                res.append(entry.getKey()).append('=').append(entry.getValue()).append('\n');
            }
            LOGGER.debug(res.toString());
        }
    }

    protected void loadProperties(Properties properties, ClassLoader classLoader, String resource) {
        try (InputStream in = classLoader.getResourceAsStream(resource)) {
            if (in != null) {
                properties.load(in);
                LOGGER.debug("Successful loaded properties from classpath resource: {}", resource);
            } else {
                throw new HttpServerConfigException("Classpath resource not found: " + resource);
            }
        } catch (IOException e) {
            throw new HttpServerConfigException("Can't load properties from resource: " + resource, e);
        }
    }
    // creating server info object
    protected ServerInfo createServerInfo() {
        ServerInfo si = new ServerInfo(
                serverProperties.getProperty("server.name"),
                Integer.parseInt(serverProperties.getProperty("server.port")),
                Integer.parseInt(serverProperties.getProperty("server.thread.count")));
        // check the correctness of thread count property
        if (si.getThreadCount() < 0) {
            throw new HttpServerConfigException("server.thread.count should be >= 0");
        }
        return si;
    }
    // reading root path
    protected Path createRootPath() {
        // getting root path from property
        Path path = Paths.get(new File(this.serverProperties.getProperty("wepapp.static.dir.root")).getAbsoluteFile().toURI());
        // perform checks on the correct path and directory
        if (!Files.exists(path)) {
            throw new HttpServerConfigException("Root path not found: " + path.toString());
        }
        if (!Files.isDirectory(path)) {
            throw new HttpServerConfigException("Root path is not directory: " + path.toString());
        }
        LOGGER.info("Root path is {}", path.toAbsolutePath());
        return path;
    }
    // getting database
    protected BasicDataSource createBasicDataSource() {
        BasicDataSource ds = null;
        // checking availability of database
        if (Boolean.parseBoolean(serverProperties.getProperty("db.datasource.enabled"))) {
            ds = new BasicDataSource();
            ds.setDefaultAutoCommit(false);
            ds.setRollbackOnReturn(true);
            ds.setDriverClassName(Objects.requireNonNull(serverProperties.getProperty("db.datasource.driver")));
            ds.setUrl(Objects.requireNonNull(serverProperties.getProperty("db.datasource.url")));
            ds.setUsername(Objects.requireNonNull(serverProperties.getProperty("db.datasource.username")));
            ds.setPassword(Objects.requireNonNull(serverProperties.getProperty("db.datasource.password")));
            ds.setInitialSize(Integer.parseInt(Objects.requireNonNull(serverProperties.getProperty("db.datasource.pool.initSize"))));
            ds.setMaxTotal(Integer.parseInt(Objects.requireNonNull(serverProperties.getProperty("db.datasource.pool.maxSize"))));
            LOGGER.info("Datasource is enabled. JDBC url is {}", ds.getUrl());
        } else {
            LOGGER.info("Datasource is disabled");
        }
        return ds;
    }

    @Override
    public ServerInfo getServerInfo() {
        return serverInfo;
    }
    // request status message
    @Override
    public String getStatusMessage(int statusCode) {
        String message = statusesProperties.getProperty(String.valueOf(statusCode));
        return message != null ? message : statusesProperties.getProperty("500"); // default status is 500
    }

    @Override
    public HttpRequestParser getHttpRequestParser() {
        return httpRequestParser;
    }

    @Override
    public HttpResponseWriter getHttpResponseWriter() {
        return httpResponseWriter;
    }

    @Override
    public HttpResponseBuilder getHttpResponseBuilder() {
        return httpResponseBuilder;
    }

    @Override
    public HttpServerContext getHttpServerContext() {
        return httpServerContext;
    }

    @Override
    public HttpRequestDispatcher getHttpRequestDispatcher() {
        return httpRequestDispatcher;
    }

    @Override
    public ThreadFactory getWorkerThreadFactory() {
        return workerThreadFactory;
    }
    // building new socket
    @Override
    public HttpClientSocketHandler buildNewHttpClientSocketHandler(Socket clientSocket) {
        return new DefaultHttpClientSocketHandler(clientSocket, this);
    }

    protected Properties getServerProperties() {
        return serverProperties;
    }

    protected Properties getStatusesProperties() {
        return statusesProperties;
    }

    protected Properties getMimeTypesProperties() {
        return mimeTypesProperties;
    }

    protected Map<String, HttpHandler> getHttpHandlers() {
        return httpHandlers;
    }

    protected BasicDataSource getDataSource() {
        return dataSource;
    }

    protected Path getRootPath() {
        return rootPath;
    }

    protected HttpHandler getDefaultHttpHandler() {
        return defaultHttpHandler;
    }

    protected HtmlTemplateManager getHtmlTemplateManager() {
        return htmlTemplateManager;
    }

    protected List<String> getStaticExpiresExtensions() {
        return staticExpiresExtensions;
    }

    protected int getStaticExpiresDays() {
        return staticExpiresDays;
    }
    // safety closing of database resource
    @Override
    public void close() {
        if (dataSource != null) {
            try {
                dataSource.close();
            } catch (SQLException e) {
                LOGGER.error("Close datasource failed: " + e.getMessage(), e);
            }
        }
        LOGGER.info("DefaultHttpServerConfig closed");
    }
}
