package main.java.com.server.handlers;

import java.io.PrintWriter;

public final class Writifier {
    public static void systemWriter(PrintWriter pw, String message) {
        pw.println("<SYSTEM> " + message);
    }

    public static void messageWriter(PrintWriter pw, String username, String message) {
        pw.println("[" + username + "] " + message);
    }
}
