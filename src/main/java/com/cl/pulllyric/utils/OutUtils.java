package com.cl.pulllyric.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author: CarterCL
 * @date: 2021/1/5 22:23
 * @description:
 */
public class OutUtils {

    private static final DateTimeFormatter BASE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void info(String msg) {
        System.out.printf("%s - [INFO] - %s%n", BASE_FORMATTER.format(LocalDateTime.now()), msg);
    }

    public static void warn(String msg) {
        System.out.printf("%s - [WARN] - %s%n", BASE_FORMATTER.format(LocalDateTime.now()), msg);
    }

    public static void error(String msg) {
        System.out.printf("%s - [ERROR] - %s%n", BASE_FORMATTER.format(LocalDateTime.now()), msg);
    }

    public static void out(String msg) {
        System.out.println(msg);
    }

    private OutUtils() {
    }
}
