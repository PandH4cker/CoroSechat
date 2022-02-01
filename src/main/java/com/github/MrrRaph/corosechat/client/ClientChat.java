package com.github.MrrRaph.corosechat.client;

import com.github.MrrRaph.corosechat.logger.Logger;
import com.github.MrrRaph.corosechat.logger.LoggerFactory;
import com.github.MrrRaph.corosechat.server.ServerChat;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

import static java.nio.file.StandardOpenOption.*;

public class ClientChat extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(ServerChat.class.getSimpleName());

    private final static String DIR_PATH= "download/";

    private Socket socket;
    private PrintWriter out;
    private Scanner in;

    private static boolean LOOP = true;

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
        while (LOOP) {
            if (!this.socket.isClosed()) {
                String input = sc.nextLine();
                if (input.toLowerCase().startsWith("/sendfile")) {
                    String[] splittedInput = input.split(" ");
                    if (splittedInput.length == 3) {
                        String filename = splittedInput[2];
                        if (Files.exists(Paths.get(filename))) {
                            try {
                                long fileSize = Files.size(Paths.get(filename));
                                if (fileSize > 0) {
                                    this.out.println(input + " " + fileSize);
                                    for (byte b : Files.readAllBytes(Paths.get(filename))) {
                                        this.out.write(b);
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else System.out.println("The file specified does not exist !");
                    }
                } else if (input.startsWith("/exit") || input.startsWith("/logout")) {
                    this.out.println(input);

                    try {
                        this.socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    LOOP = false;
                } else this.out.println(input);
            }
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
        while (LOOP) {
            if (this.in.hasNextLine()) {
                String serverOutput = this.in.nextLine();
                System.out.println(serverOutput);
                if (serverOutput.startsWith("<SYSTEM>")) {
                    if (serverOutput.endsWith("created!")) {
                        try {
                            this.socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        LOOP = false;
                    } else if (serverOutput.endsWith("/enable ftm")) {
                        //fileTransferMode = true;
                        System.out.println("[+] File Transfer Mode enabled.");
                        String filename = this.in.nextLine().trim();
                        if (Files.exists(Paths.get(DIR_PATH + filename))) {
                            try {
                                Files.createDirectories(Paths.get(DIR_PATH));
                                Files.delete(Paths.get(DIR_PATH + filename));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        while (this.in.hasNextLine()) {
                            int input = Integer.parseInt(this.in.nextLine()) & 0xFF;
                            if (input == 0xFF) break;
                            else {
                                try {
                                    Files.write(Paths.get(DIR_PATH + filename), new byte[]{(byte) input}, APPEND, CREATE);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            //else System.out.print((char) input);
                        }
                        System.out.println("[-] File Transfer disabled.");
                    }
                }
            }
        }
    }
}
