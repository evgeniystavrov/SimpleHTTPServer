package ru.evgs.httpserver.io;

import java.util.Map;

// interface that describing template processing
public interface HtmlTemplateManager {
    // template processing for conversion to html format
    String processTemplate(String templateName, Map<String, Object> args);
}
