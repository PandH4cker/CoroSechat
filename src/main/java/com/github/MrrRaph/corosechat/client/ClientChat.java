package com.github.MrrRaph.corosechat.client;

import com.github.MrrRaph.corosechat.logger.Logger;
import com.github.MrrRaph.corosechat.logger.LoggerFactory;
import com.github.MrrRaph.corosechat.server.ServerChat;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientChat extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(ServerChat.class.getSimpleName());

    private Socket socket;
    private PrintWriter out;
    private Scanner in;

    public ClientChat(String[] args) {
        this.initStreams(args);
        this.start();
        this.listenConsole();
    }

    private void listenConsole() {

    }

    private void initStreams(String[] args) {
        try {
            this.socket = new Socket(args[1], Integer.parseInt(args[2]));
            this.in = new Scanner(this.socket.getInputStream());
            this.out = new PrintWriter(this.socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length >= 3)
            new ClientChat(args);
    }

    @Override
    public void run() {
    }
}
