package ru.evgs.httpserver.io.impl;

import org.apache.commons.io.IOUtils;
import ru.evgs.httpserver.io.HtmlTemplateManager;
import ru.evgs.httpserver.io.exception.HttpServerException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
// implementation of template processing
public class DefaultHtmlTemplateManager implements HtmlTemplateManager {
    // map for storing templates
    private final Map<String, String> templates = new HashMap<>();

    @Override
    public String processTemplate(String templateName, Map<String, Object> args) {
        // getting template by name
        String template = getTemplate(templateName);
        return populateTemplate(template, args);
    }
    // getting input stream by classname
    protected InputStream getClasspathResource(String name) {
        return DefaultHtmlTemplateManager.class.getClassLoader().getResourceAsStream(name);
    }
    // method for getting template
    protected String getTemplate(String templateName) {
        // getting template from map
        String template = templates.get(templateName);
        if (template == null) {
            // try to get template from classpath
            try (InputStream in = getClasspathResource("html/templates/" + templateName)) {
                if (in == null) {
                    throw new HttpServerException("Classpath resource \"html/templates/" + templateName + "\" not found");
                }
                template = IOUtils.toString(in, StandardCharsets.UTF_8);
                // putting template to map by name
                templates.put(templateName, template);
            } catch (IOException e) {
                throw new HttpServerException("Can't load template: " + templateName, e);
            }
        }
        return template;
    }
    // forming html
    protected String populateTemplate(String template, Map<String, Object> args) {
        String html = template;
        for (Map.Entry<String, Object> entry : args.entrySet()) {
            html = html.replace("${" + entry.getKey() + "}", String.valueOf(entry.getValue()));
        }
        return html;
    }
}
