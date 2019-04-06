package ru.evgs.httpserver.io.impl;

import ru.evgs.httpserver.io.Constants;
import ru.evgs.httpserver.io.HttpRequest;
import ru.evgs.httpserver.io.config.HttpRequestParser;
import ru.evgs.httpserver.io.exception.BadRequestException;
import ru.evgs.httpserver.io.exception.HttpServerException;
import ru.evgs.httpserver.io.exception.HttpVersionNotSupportedException;
import ru.evgs.httpserver.io.exception.MethodNotAllowedException;
import ru.evgs.httpserver.io.utils.DataUtils;
import ru.evgs.httpserver.io.utils.HttpUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

// class that implements parsing http request
// class can be used only within the package
class DefaultHttpRequestParser implements HttpRequestParser {
    // proper main method parsing request
    @Override
    public HttpRequest parseHttpRequest(InputStream inputStream, String remoteAddress) throws HttpServerException, IOException {
        String startingLine = null;
        try {
            // parsing incoming stream to inner class
            ParsedRequest request = parseInputStream(inputStream);
            return convertParsedRequestToHttpRequest(request, remoteAddress);
        } catch (RuntimeException e) {
            if (e instanceof HttpServerException) {
                throw e;
            } else {
                throw new BadRequestException("Can't parse http request: " + e.getMessage(), e, startingLine);
            }
        }
    }

    protected ParsedRequest parseInputStream(InputStream inputStream) throws IOException {
        String startingLineAndHeaders = HttpUtils.readStartingLineAndHeaders(inputStream);
        // we look for whether there is a header "content-length: " in the line
        int contentLengthIndex = HttpUtils.getContentLengthIndex(startingLineAndHeaders);
        // if string have it
        if (contentLengthIndex != -1) {
            // extracting value
            int contentLength = HttpUtils.getContentLengthValue(startingLineAndHeaders, contentLengthIndex);
            // and reading message body of request
            String messageBody = HttpUtils.readMessageBody(inputStream, contentLength);
            // returning parsed request with body
            return new ParsedRequest(startingLineAndHeaders, messageBody);
        } else {
            // or without if string have no content-length
            return new ParsedRequest(startingLineAndHeaders, null);
        }
    }

    protected HttpRequest convertParsedRequestToHttpRequest(ParsedRequest request, String remoteAddress) throws IOException {
        // parse starting line: GET /index.html HTTP/1.1
        String[] startingLineData = request.startingLine.split(" "); // by spaces
        String method = startingLineData[0];
        String uri = startingLineData[1];
        String httpVersion = startingLineData[2];
        // validating
        validateHttpVersion(request.startingLine, httpVersion);
        // parse headers: Host: localhost
        Map<String, String> headers = parseHeaders(request.headersLine);
        // parse message body or uri params
        ProcessedUri processedUri = extractParametersIfPresent(method, uri, httpVersion, request.messageBody);
        return new DefaultHttpRequest(method, processedUri.uri, httpVersion, remoteAddress, headers, processedUri.parameters);
    }
    // check the protocol version against the supported constant
    protected void validateHttpVersion(String startingLine, String httpVersion) {
        if (!Constants.HTTP_VERSION.equals(httpVersion)) {
            throw new HttpVersionNotSupportedException("Current server supports only " + Constants.HTTP_VERSION + " protocol", startingLine);
        }
    }
    // parsing headers
    protected Map<String, String> parseHeaders(List<String> list) throws IOException {
        // creating map for them
        Map<String, String> map = new LinkedHashMap<>();
        // defining variable for previous header name
        String prevName = null;
        // go through the list, add headings to the map
        for(String headerItem : list) {
            prevName = putHeader(prevName, map, headerItem);
        }
        return map;
    }

    protected String putHeader(String prevName, Map<String, String> map, String header) {
        // if the line starts with a space, then this is the value that refers to the previous header
        if (header.charAt(0) == ' ') {
            String value = map.get(prevName) + header.trim();
            map.put(prevName, value);
            return prevName;
        } else {
            // if not, we read the header name and value, putting them in the map
            int index = header.indexOf(':');
            String name = HttpUtils.normilizeHeaderName(header.substring(0, index));
            String value = header.substring(index + 1).trim();
            map.put(name, value);
            return name;
        }
    }

    protected ProcessedUri extractParametersIfPresent(String method, String uri, String httpVersion, String messageBody) throws IOException {
        Map<String, String> parameters = Collections.emptyMap();
        // we look whether the request contains valid methods
        if (Constants.GET.equalsIgnoreCase(method) || Constants.HEAD.equalsIgnoreCase(method)) {
            int indexOfDelim = uri.indexOf('?'); // delimiter
            if (indexOfDelim != -1) {
                return extractParametersFromUri(uri, indexOfDelim);
            }
        } else if (Constants.POST.equalsIgnoreCase(method)) {
            if(messageBody != null && !"".equals(messageBody)) {
                parameters = getParameters(messageBody);
            }
        } else {
            // if not, we handle the error due to unsupported methods.
            throw new MethodNotAllowedException(method, String.format("%s %s %s", method, uri, httpVersion));
        }
        return new ProcessedUri(uri, parameters);
    }
    // extract parameters by delimiter
    protected ProcessedUri extractParametersFromUri(String uri, int indexOfDelim) throws UnsupportedEncodingException {
        String paramString = uri.substring(indexOfDelim + 1);
        Map<String, String> parameters = getParameters(paramString);
        uri = uri.substring(0, indexOfDelim);
        return new ProcessedUri(uri, parameters);
    }
    // method that searching parameters by delimiters
    protected Map<String, String> getParameters(String paramString) throws UnsupportedEncodingException {
        Map<String, String> map = new HashMap<>();
        String[] params = paramString.split("&");
        for (String param : params) {
            String[] items = param.split("=");
            // if empty value for param
            if (items.length == 1) {
                items = new String[] { items[0], "" };
            }
            String name = items[0];
            String value = map.get(name);
            if (value != null) {
                value += "," + URLDecoder.decode(items[1], "UTF-8");
            } else {
                value = URLDecoder.decode(items[1], "UTF-8");
            }
            map.put(name, value);
        }
        return map;
    }
    // inner class that storing parsed request
    private static class ParsedRequest {
        private final String startingLine;
        private final List<String> headersLine;
        private final String messageBody;
        public ParsedRequest(String startingLineAndHeaders, String messageBody) {
            super();
            List<String> list = DataUtils.convertToLineList(startingLineAndHeaders);
            // extract the starting line
            this.startingLine = list.remove(0);
            // if list is empty...
            if(list.isEmpty()) {
                // assign an empty header list
                this.headersLine = Collections.emptyList();
            } else {
                // if it not, wrap in a non-modifiable list
                this.headersLine = Collections.unmodifiableList(list);
            }
            this.messageBody = messageBody;
        }
    }
    // inner class for storing uri and it's parameters
    private static class ProcessedUri {
        final String uri;
        final Map<String, String> parameters;

        ProcessedUri(String uri, Map<String, String> parameters) {
            super();
            this.uri = uri;
            this.parameters = parameters;
        }
    }
}
