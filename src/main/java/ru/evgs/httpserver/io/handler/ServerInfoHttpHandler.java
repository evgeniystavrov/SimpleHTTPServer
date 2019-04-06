package ru.evgs.httpserver.io.handler;

import ru.evgs.httpserver.io.*;
import ru.evgs.httpserver.io.utils.DataUtils;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class ServerInfoHttpHandler implements HttpHandler {

    @Override
    public void handle(HttpServerContext context, HttpRequest request, HttpResponse response) throws IOException {
        if (Constants.GET.equals(request.getMethod())) {
            // getting map with arguments
            Map<String, Object> args = getDataMap(context);
            response.setBody(context.getHtmlTemplateManager().processTemplate("server-info.html", args));
        } else {
            response.setStatus(400);
        }
    }

    protected Map<String, Object> getDataMap(HttpServerContext context) {
        int threadCount = context.getServerInfo().getThreadCount();
        // building map with server parameters
        return DataUtils.buildMap(new Object[][] {
                { "SERVER-NAME",  context.getServerInfo().getName() },
                { "SERVER-PORT",  context.getServerInfo().getPort() },
                { "THREAD-COUNT", threadCount == 0 ? "UNLIMITED" : threadCount },
                { "SUPPORTED-REQUEST-METHODS",   context.getSupportedRequestMethods() },
                { "SUPPORTED-RESPONSE-STATUSES", getSupportedResponseStatuses(context) }
        });
    }

    protected StringBuilder getSupportedResponseStatuses(HttpServerContext context) {
        StringBuilder s = new StringBuilder();
        Map<Object, Object> map = new TreeMap<>(context.getSupportedResponseStatuses());
        // building strings with statuses
        for(Map.Entry<Object, Object> entry : map.entrySet()) {
            s.append(entry.getKey()).append(" [").append(entry.getValue()).append("]<br>");
        }
        return s;
    }
}
