package com.github.MrrRaph.corosechat.server.utils;

public final class StringUtils {
    public static String removeNonPrintable(String s) {
        return s.replaceAll("\\p{C}", "");
    }
}
