package ru.evgs.httpserver.io.utils;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

// utility class that provides methods for handling an http request
public final class HttpUtils {
    // normalizes the header according to the rules of http headers
    public static String normilizeHeaderName(String name) {
        StringBuilder headerName = new StringBuilder(name.trim());
        for (int i = 0; i < headerName.length(); i++) {
            char ch = headerName.charAt(i);
            if (i == 0) {
                toUpper(ch, i, headerName);
            } else if (ch == '-' && i < headerName.length() - 1) {
                toUpper(headerName.charAt(i + 1), i + 1, headerName);
                i++;
            } else {
                if (Character.isUpperCase(ch)) {
                    headerName.setCharAt(i, Character.toLowerCase(ch));
                }
            }
        }
        return headerName.toString();
    }

    private static void toUpper(char ch, int index, StringBuilder headerName) {
        if (Character.isLowerCase(ch)) {
            headerName.setCharAt(index, Character.toUpperCase(ch));
        }
    }

    public static String readStartingLineAndHeaders(InputStream inputStream) throws IOException {
        ByteArray byteArray = new ByteArray();
        while (true) {
            // reading
            int read = inputStream.read();
            // if read returns -1 throws an exception error with message
            if (read == -1) {
                throw new EOFException("InputStream closed");
            }
            // adding bytes to array
            byteArray.add((byte) read);
            // if line is empty stop reading
            if (byteArray.isEmptyLine()) {
                break;
            }
        }
        // returning string that have starting line and headers
        return new String(byteArray.toArray(), StandardCharsets.UTF_8);
    }
    // method for reading message body form request
    public static String readMessageBody(InputStream inputStream, int contentLength) throws IOException {
        // creating variable for body
        StringBuilder messageBody = new StringBuilder();
        while (contentLength > 0) {
            // creating array for body bytes
            byte[] messageBytes = new byte[contentLength];
            // reading
            int read = inputStream.read(messageBytes);
            // appending read bytes to body
            messageBody.append(new String(messageBytes, 0, read, StandardCharsets.UTF_8));
            // reduce body size by the number of bytes read
            contentLength -= read;
        }
        return messageBody.toString();
    }

    public static int getContentLengthIndex(String startingLineAndHeaders) {
        return startingLineAndHeaders.toLowerCase().indexOf(CONTENT_LENGTH);
    }
    // method for extracting content-length value
    public static int getContentLengthValue(String startingLineAndHeaders, int contentLengthIndex) {
        // get start reading
        int startCutIndex = contentLengthIndex + CONTENT_LENGTH.length();
        // get end reading
        int endCutIndex = startingLineAndHeaders.indexOf("\r\n", startCutIndex);
        // if there is no line break, then set the end of the read to the end
        if(endCutIndex == -1) {
            endCutIndex = startingLineAndHeaders.length();
        }
        // return value
        return Integer.parseInt(startingLineAndHeaders.substring(startCutIndex, endCutIndex).trim());
    }
    // constant for searching content-length
    private static final String CONTENT_LENGTH = "content-length: ";
    // inner class that implements a dynamic byte array
    private static class ByteArray {
        private byte[] array = new byte[1024];
        private int size;
        // adding with expansion of internal static array
        private void add(byte value) {
            if (size == array.length) {
                byte[] temp = array;
                array = new byte[array.length * 2];
                System.arraycopy(temp, 0, array, 0, size);
            }
            array[size++] = value;
        }

        private byte[] toArray() {
            if(size > 4) {
                return Arrays.copyOf(array, size - 4);
            } else {
                throw new IllegalStateException("Byte array has invalid size: " + Arrays.toString(Arrays.copyOf(array, size)));
            }
        }

        private boolean isEmptyLine() {
            if (size >= 4) {
                return array[size - 1] == '\n' && array[size - 2] == '\r' && array[size - 3] == '\n' && array[size - 4] == '\r';
            } else {
                return false;
            }
        }
    }

    private HttpUtils() {
    }
}
