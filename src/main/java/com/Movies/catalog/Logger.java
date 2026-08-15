package com.Movies.catalog;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void info(String message) {
        System.out.println("[" + LocalDateTime.now().format(FORMATTER) + "] [INFO] " + message);
    }

    public static void error(String message) {
        System.err.println("[" + LocalDateTime.now().format(FORMATTER) + "] [ERROR] " + message);
    }

    public static void debug(String message) {
        System.out.println("[" + LocalDateTime.now().format(FORMATTER) + "] [DEBUG] " + message);
    }
}
