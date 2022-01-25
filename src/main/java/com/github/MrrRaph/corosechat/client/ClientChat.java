package com.github.MrrRaph.corosechat.client;

import com.github.MrrRaph.corosechat.logger.Logger;
import com.github.MrrRaph.corosechat.logger.LoggerFactory;
import com.github.MrrRaph.corosechat.server.ServerChat;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
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

    public static void main(String[] args) {
        if (args.length >= 2) new ClientChat(args);
    }

    private void listenConsole() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            String input = sc.nextLine();
            if (input.startsWith("/sendFile")) {
                String[] splittedInput = input.split(" ");
                if (splittedInput.length == 3) {
                    String filename = splittedInput[2];
                    this.out.println(input);
                    try {
                        for (byte b : Files.readAllBytes(Paths.get(filename))) {
                            this.out.write(b);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else this.out.println(input);
        }
    }

    private void initStreams(String[] args) {
        try {
            this.socket = new Socket(args[0], Integer.parseInt(args[1]));
            this.in = new Scanner(this.socket.getInputStream());
            this.out = new PrintWriter(this.socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        listenNetwork();
    }

    private void listenNetwork() {
        while (true) {
            if (this.in.hasNextLine()) {
                System.out.println(this.in.nextLine());
            }
        }
    }
}
