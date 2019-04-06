package ru.evgs.httpserver.io;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Properties;

// interface defining methods for receiving information by the dispatch handler
public interface HttpServerContext {
    // returns server information
    ServerInfo getServerInfo();
    // returns a collection of supported methods
    Collection<String> getSupportedRequestMethods();
    // returns supported response statuses
    Properties getSupportedResponseStatuses();
    // returns database information
    DataSource getDataSource();

    Path getRootPath();
    // returns information about supported mime-types
    String getContentType(String extension);
    // provides access to the current template handler
    HtmlTemplateManager getHtmlTemplateManager();
    // returns information about the number of days of caching static resources
    Integer getExpiresDaysForResource(String extension);
}
