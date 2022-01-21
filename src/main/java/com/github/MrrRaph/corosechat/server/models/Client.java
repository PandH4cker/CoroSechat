package com.github.MrrRaph.corosechat.server.models;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Client {
    private Socket socket;

    public Client(final Socket socket) {
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public PrintWriter getWriter() {
        try {
            return new PrintWriter(
                    new OutputStreamWriter(this.socket.getOutputStream(), StandardCharsets.UTF_8),
                    true
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
