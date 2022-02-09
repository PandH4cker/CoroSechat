package com.github.MrrRaph.corosechat.client;

import com.github.MrrRaph.corosechat.logger.Logger;
import com.github.MrrRaph.corosechat.logger.LoggerFactory;
import com.github.MrrRaph.corosechat.server.ServerChat;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Scanner;

import static java.nio.file.StandardOpenOption.*;

public class ClientChat extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(ServerChat.class.getSimpleName());

    private final static String DIR_PATH= "download/";

    private Socket socket;
    private PrintWriter out;
    private Scanner in;
    private PrivateKey privateKey;
    private Cipher cipher;

    private static boolean LOOP = true;

    private static final byte[] MODULUS_B = new byte[] {
            (byte)0x90,(byte)0x08,(byte)0x15,(byte)0x32,(byte)0xb3,(byte)0x6a,(byte)0x20,(byte)0x2f,
            (byte)0x40,(byte)0xa7,(byte)0xe8,(byte)0x02,(byte)0xac,(byte)0x5d,(byte)0xec,(byte)0x11,
            (byte)0x1d,(byte)0xfa,(byte)0xf0,(byte)0x6b,(byte)0x1c,(byte)0xb7,(byte)0xa8,(byte)0x39,
            (byte)0x19,(byte)0x50,(byte)0x9c,(byte)0x44,(byte)0xed,(byte)0xa9,(byte)0x51,(byte)0x01,
            (byte)0x0f,(byte)0x11,(byte)0xd6,(byte)0xa3,(byte)0x60,(byte)0xa7,(byte)0x7e,(byte)0x95,
            (byte)0xa2,(byte)0xfa,(byte)0xe0,(byte)0x8d,(byte)0x62,(byte)0x5b,(byte)0xf2,(byte)0x62,
            (byte)0xa2,(byte)0x64,(byte)0xfb,(byte)0x39,(byte)0xb0,(byte)0xf0,(byte)0x6f,(byte)0xa2,
            (byte)0x23,(byte)0xae,(byte)0xbc,(byte)0x5d,(byte)0xd0,(byte)0x1a,(byte)0x68,(byte)0x11,
            (byte)0xa7,(byte)0xc7,(byte)0x1b,(byte)0xda,(byte)0x17,(byte)0xc7,(byte)0x14,(byte)0xab,
            (byte)0x25,(byte)0x92,(byte)0xbf,(byte)0xcc,(byte)0x81,(byte)0x65,(byte)0x7a,(byte)0x08,
            (byte)0x90,(byte)0x59,(byte)0x7f,(byte)0xc4,(byte)0xf9,(byte)0x43,(byte)0x9c,(byte)0xaa,
            (byte)0xbe,(byte)0xe4,(byte)0xf8,(byte)0xfb,(byte)0x03,(byte)0x74,(byte)0x3d,(byte)0xfb,
            (byte)0x59,(byte)0x7a,(byte)0x56,(byte)0xa3,(byte)0x19,(byte)0x66,(byte)0x43,(byte)0x77,
            (byte)0xcc,(byte)0x5a,(byte)0xae,(byte)0x21,(byte)0xf5,(byte)0x20,(byte)0xa1,(byte)0x22,
            (byte)0x8f,(byte)0x3c,(byte)0xdf,(byte)0xd2,(byte)0x03,(byte)0xe9,(byte)0xc2,(byte)0x38,
            (byte)0xe7,(byte)0xd9,(byte)0x38,(byte)0xef,(byte)0x35,(byte)0x82,(byte)0x48,(byte)0xb7
    };

    private static final byte[] PRIVATE_EXPONENT_B = new byte[] {
            (byte)0x69,(byte)0xdf,(byte)0x67,(byte)0x25,(byte)0xa3,(byte)0xb8,(byte)0x88,(byte)0xfb,
            (byte)0xf2,(byte)0xfc,(byte)0xf9,(byte)0x90,(byte)0xad,(byte)0x7f,(byte)0x44,(byte)0xbd,
            (byte)0xb8,(byte)0x59,(byte)0xf3,(byte)0x4b,(byte)0xe9,(byte)0x0a,(byte)0x1f,(byte)0x80,
            (byte)0x09,(byte)0x59,(byte)0xb5,(byte)0xe4,(byte)0xfd,(byte)0x06,(byte)0x0e,(byte)0xe3,
            (byte)0x46,(byte)0x5e,(byte)0x88,(byte)0x76,(byte)0x03,(byte)0xe0,(byte)0x5b,(byte)0x2e,
            (byte)0x47,(byte)0x65,(byte)0x3e,(byte)0x96,(byte)0xef,(byte)0x0c,(byte)0x43,(byte)0x79,
            (byte)0xb9,(byte)0x81,(byte)0x9d,(byte)0x21,(byte)0xe5,(byte)0x2c,(byte)0x78,(byte)0x02,
            (byte)0xa9,(byte)0x54,(byte)0x12,(byte)0x66,(byte)0xab,(byte)0x48,(byte)0x1d,(byte)0xe2,
            (byte)0x6e,(byte)0x1d,(byte)0x7d,(byte)0xb2,(byte)0xce,(byte)0x7a,(byte)0x3f,(byte)0xbb,
            (byte)0x34,(byte)0xf2,(byte)0x46,(byte)0x5f,(byte)0x73,(byte)0x7c,(byte)0xba,(byte)0xf8,
            (byte)0xc1,(byte)0x29,(byte)0x97,(byte)0x85,(byte)0x67,(byte)0xdf,(byte)0x82,(byte)0x87,
            (byte)0x89,(byte)0x61,(byte)0x42,(byte)0xcc,(byte)0x1d,(byte)0xcc,(byte)0x03,(byte)0xce,
            (byte)0x41,(byte)0x7d,(byte)0x8f,(byte)0x25,(byte)0xc1,(byte)0x61,(byte)0xfe,(byte)0x06,
            (byte)0x4f,(byte)0x1a,(byte)0xf2,(byte)0x48,(byte)0x55,(byte)0xd8,(byte)0x6e,(byte)0xc6,
            (byte)0x3f,(byte)0x6d,(byte)0xe1,(byte)0xce,(byte)0xa9,(byte)0x28,(byte)0x9e,(byte)0x03,
            (byte)0x2d,(byte)0x74,(byte)0x59,(byte)0x1c,(byte)0xdb,(byte)0x18,(byte)0xb3,(byte)0x41
    };

    public ClientChat(String[] args) {
        String mod_s = new String(Hex.encodeHex(MODULUS_B));
        String priv_s = new String(Hex.encodeHex(PRIVATE_EXPONENT_B));

        BigInteger modulus = new BigInteger(mod_s, 16);
        BigInteger privExponent = new BigInteger(priv_s, 16);

        RSAPrivateKeySpec privateSpec = new RSAPrivateKeySpec(modulus, privExponent);
        try {
            KeyFactory factory = KeyFactory.getInstance("RSA");
            this.privateKey = factory.generatePrivate(privateSpec);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        Security.addProvider(new BouncyCastleProvider());
        try {
            this.cipher = Cipher.getInstance("RSA/NONE/NoPadding", "BC");
            this.cipher.init(Cipher.DECRYPT_MODE, this.privateKey);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }

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
            } else LOOP = false;
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
                    if (serverOutput.endsWith("created!") || serverOutput.endsWith("Socket closed.")) {
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
                                    Files.createDirectories(Paths.get(DIR_PATH));
                                    Files.write(Paths.get(DIR_PATH + filename), new byte[]{(byte) input}, APPEND, CREATE);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        System.out.println("[-] File Transfer disabled.");
                    } else if (serverOutput.startsWith("<SYSTEM> Challenge:")) {
                        String[] splittedServerOutput = serverOutput.split(" ");
                        String cmd = splittedServerOutput[1];
                        String challenge = cmd.split(":")[1];
                        System.out.println("Got a challenge: " + challenge);

                        byte[] challengeByte = Base64.decodeBase64(challenge.getBytes());
                        try {
                            byte[] unciphered = this.cipher.doFinal(challengeByte);
                            this.out.println(new String(Base64.encodeBase64(unciphered)));
                        } catch (IllegalBlockSizeException | BadPaddingException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
