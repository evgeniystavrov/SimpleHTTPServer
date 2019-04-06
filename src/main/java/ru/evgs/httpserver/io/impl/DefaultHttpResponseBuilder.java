package ru.evgs.httpserver.io.impl;

import ru.evgs.httpserver.io.config.HttpResponseBuilder;
import ru.evgs.httpserver.io.config.HttpServerConfig;
import ru.evgs.httpserver.io.config.ReadableHttpResponse;
import ru.evgs.httpserver.io.utils.DataUtils;

import java.util.Date;
import java.util.Map;
// class that creating empty http response for request dispatcher
class DefaultHttpResponseBuilder extends AbstractHttpConfigurableComponent implements HttpResponseBuilder {
    // in the constructor, we access to the object server config to retrieve server parameters
    DefaultHttpResponseBuilder(HttpServerConfig httpServerConfig) {
        super(httpServerConfig);
    }

    protected ReadableHttpResponse createReadableHttpResponseInstance(){
        return new DefaultReadableHttpResponse();
    }
    // method that creating empty http response
    @Override
    public ReadableHttpResponse buildNewHttpResponse() {
        ReadableHttpResponse response = createReadableHttpResponseInstance();
        response.setHeader("Date", new Date());
        response.setHeader("Server", httpServerConfig.getServerInfo().getName());
        response.setHeader("Content-Language", "en");
        response.setHeader("Connection", "close");
        response.setHeader("Content-Type", "text/html");// default content type
        return response;
    }
    // method for prepare http response
    @Override
    public void prepareHttpResponse(ReadableHttpResponse response, boolean clearBody) {
        if (response.getStatus() >= 400 && response.isBodyEmpty()) {
            setDefaultResponseErrorBody(response);
        }
        setContentLength(response);
        if (clearBody) {
            clearBody(response);
        }
    }
    // method that prepare error page
    protected void setDefaultResponseErrorBody(ReadableHttpResponse response) {
        Map<String, Object> args = DataUtils.buildMap(new Object[][] {
                { "STATUS-CODE", response.getStatus() },
                { "STATUS-MESSAGE", httpServerConfig.getStatusMessage(response.getStatus()) }
        });
        String content = httpServerConfig.getHttpServerContext().getHtmlTemplateManager().processTemplate("error.html", args);
        response.setBody(content);
    }

    protected void setContentLength(ReadableHttpResponse response) {
        response.setHeader("Content-Length", response.getBodyLength());
    }
    // for support HEAD method
    protected void clearBody(ReadableHttpResponse response) {
        response.setBody("");
    }
}
