package uk.ac.comm2020.util;

public class JsonUtil {

    public static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}