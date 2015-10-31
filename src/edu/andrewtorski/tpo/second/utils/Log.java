package edu.andrewtorski.tpo.second.utils;

public class Log {

    public static void d(String tag, String message) {
        System.out.println(tag + ": " + message);
    }

    public static void e(String tag, String message) {
        System.err.println(tag + ": " + message);
    }


}
