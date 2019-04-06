package ru.evgs.httpserver.io.utils;

import java.util.*;

// utility class for data processing
public final class DataUtils {

    public static Map<String, Object> buildMap(Object[][] data) {
        Map<String, Object> map = new HashMap<>();
        for(Object[] row : data) {
            map.put(String.valueOf(row[0]), row[1]);
        }
        return Collections.unmodifiableMap(map);
    }
    // utility method separating the starting line and headers
    public static List<String> convertToLineList(String message) {
        List<String> list = new LinkedList<>();
        int start = 0;
        for (int i = 1; i < message.length(); i++) {
            if (message.charAt(i) == '\n' && message.charAt(i - 1) == '\r') {
                list.add(message.substring(start, i - 1));
                start = i + 1;
            }
        }
        if (message.length() > 0) {
            list.add(message.substring(start));
        }
        return list;
    }

    private DataUtils(){
    }
}
