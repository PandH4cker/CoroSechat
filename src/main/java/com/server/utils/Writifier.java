package main.java.com.server.utils;

import java.io.PrintWriter;

public final class Writifier {
    public static void systemWriter(PrintWriter pw, String message) {
        pw.println("<SYSTEM> " + message);
    }

    public static void messageWriter(PrintWriter pw, String username, String message, Boolean isAdmin) {
        if (!isAdmin) pw.println("[" + username + "] " + message);
        else pw.println(username + " " + message);
    }
}
