package ru.evgs.httpserver.io.impl;

import org.apache.commons.io.FilenameUtils;
import ru.evgs.httpserver.io.HttpHandler;
import ru.evgs.httpserver.io.HttpRequest;
import ru.evgs.httpserver.io.HttpResponse;
import ru.evgs.httpserver.io.HttpServerContext;
import ru.evgs.httpserver.io.utils.DataUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;
// class that implements the display of resources from the folder root
public class DefaultHttpHandler implements HttpHandler {
    @Override
    public void handle(HttpServerContext context, HttpRequest request, HttpResponse response) throws IOException {
        // getting url from uri
        String url = request.getUri();
        // putting url to path
        Path path = Paths.get(context.getRootPath().toString() + url);
        if (Files.exists(path)) {
            if (Files.isDirectory(path)) {
                // if directory, handle as directory
                handleDirectoryUrl(context, response, path);
            } else {
                // if file, handle as file
                handleFileUrl(context, response, path);
            }
        } else {
            // if not dir or file, give away code error 404
            response.setStatus(404);
        }
    }

    protected void handleDirectoryUrl(HttpServerContext context, HttpResponse response, Path path) throws IOException {
        String content = getResponseForDirectory(context, path);
        response.setBody(content);
    }

    protected void handleFileUrl(HttpServerContext context, HttpResponse response, Path path) throws IOException {
        setEntityHeaders(context, response, path);
        try (InputStream in = Files.newInputStream(path, StandardOpenOption.READ)) {
            response.setBody(in);
        }
    }

    protected void setEntityHeaders(HttpServerContext context, HttpResponse response, Path path) throws IOException {
        // define the file extension
        String extension = FilenameUtils.getExtension(path.toString());
        // getting mime-type
        response.setHeader("Content-Type", context.getContentType(extension));
        // setting last modified
        response.setHeader("Last-Modified", Files.getLastModifiedTime(path, LinkOption.NOFOLLOW_LINKS));
        // checking expires days settings
        Integer expiresDays = context.getExpiresDaysForResource(extension);
        // if for this extension we have expires days
        if(expiresDays != null) {
            // setting header expires
            response.setHeader("Expires", new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(expiresDays)));
        }
    }

    protected String getResponseForDirectory(HttpServerContext context, Path dir) throws IOException {
        String root = context.getRootPath().toString();
        StringBuilder htmlBody = new StringBuilder();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(dir)) {
            for (Path path : directoryStream) {
                htmlBody.append("<a href=\"").append(getHref(root, path)).append("\">").append(path.getFileName()).append("</a><br>\r\n");
            }
        }
        Map<String, Object> args = DataUtils.buildMap(new Object[][] {
                { "TITLE",   "File list for " + dir.getFileName() },
                { "HEADER", "File list for " + dir.getFileName() },
                { "BODY", 	 htmlBody }
        });
        return context.getHtmlTemplateManager().processTemplate("simple.html", args);
    }

    private String getHref(String root, Path path) {
        return path.toString().replace(root, "");
    }
}
