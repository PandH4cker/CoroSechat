package com.github.MrrRaph.corosechat.client;

import com.github.MrrRaph.corosechat.client.communications.server.ClientCommand;
import com.github.MrrRaph.corosechat.client.communications.server.ServerCommand;
import com.github.MrrRaph.corosechat.client.utils.card.ResponseAPDUtils;
import com.github.MrrRaph.corosechat.client.communications.card.codes.ResponseCode;
import com.github.MrrRaph.corosechat.client.utils.multiples.Pair;
import com.github.MrrRaph.corosechat.client.utils.padding.PKCS5;
import opencard.core.service.CardServiceException;
import opencard.core.service.SmartCard;
import opencard.core.terminal.CommandAPDU;
import opencard.core.terminal.ResponseAPDU;
import opencard.core.util.OpenCardPropertyLoadingException;
import opencard.opt.util.PassThruCardService;
import org.apache.commons.codec.binary.Base64;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;

import static com.github.MrrRaph.corosechat.client.utils.card.CardUtils.*;
import static com.github.MrrRaph.corosechat.client.communications.card.codes.CommandCode.*;
import static com.github.MrrRaph.corosechat.client.utils.card.rsa.RSAUtils.*;
import static java.nio.file.StandardOpenOption.*;

public class ClientChat extends Thread {
    private final static String DIR_PATH    = "download/";
    private final static short DMS          = 0xF0;
    public static final String SERVER_TAG   = "<SYSTEM>";
    public static final String ADMIN_TAG    = "<ADMIN>";
    private static final short FTM_ENDER    = 0xFF;

    private Socket socket;
    private PrintWriter out;
    private Scanner in;
    private PassThruCardService cardService;
    private boolean isConnected;
    private final byte[] modulus;
    private final byte[] publicExponent;

    private static boolean LOOP = true;


    public ClientChat(String[] args) throws IOException {
        try {
            this.cardService = getCardService();
        } catch (OpenCardPropertyLoadingException | CardServiceException | ClassNotFoundException e) {
            System.out.println("TheClient error: " + e.getMessage());
            System.exit(1);
        }

        Pair<byte[], byte[]> modulusExponent = getPublicRSAKey(this.cardService);
        this.modulus = modulusExponent.getFirst();
        this.publicExponent = modulusExponent.getSecond();
        this.isConnected = false;

        this.initStreams(args);
        this.start();
        this.listenConsole();
        SmartCard.shutdown();
    }

    public static void main(String[] args) throws IOException {
        if (args.length >= 2) new ClientChat(args);
    }

    private void listenConsole() throws IOException {
        Scanner sc = new Scanner(System.in);
        while (LOOP) {
            if (!this.socket.isClosed()) {
                String input = sc.nextLine();
                if (input.startsWith("/")) {
                    String[] splittedInput = input.split(" ", 2);
                    switch (ClientCommand.fromString(splittedInput[0])) {
                        case SEND_FILE -> sendFile(input);
                        case EXIT, LOGOUT -> logout(input);
                        case LIST, HELP -> this.out.println(input);
                        case PRIVATE_MESSAGE -> privateMessage(input);
                        default -> System.out.println("[-] Unknown command: " + input);
                    }
                } else if (!input.trim().isEmpty()) {
                    if (isConnected) this.out.println(cipheredInput(input));
                    else this.out.println(input);
                }
            } else LOOP = false;
        }
    }

    private void privateMessage(String input) throws IOException {
        String[] splittedInput = input.split(" ");
        String message = cipheredInput(String.join(
                " ",
                Arrays.copyOfRange(splittedInput, 2, splittedInput.length)
        ));
        this.out.println(String.join(" ", new String[]{splittedInput[0], splittedInput[1], message}));
    }

    private void logout(String input) throws IOException {
        this.out.println(input);
        LOOP = false;
        this.socket.close();
        System.out.println("[+] Connection closed by foreign host.");
    }

    private void logout() throws IOException {
        LOOP = false;
        this.socket.close();
        System.out.println("[+] Connection closed by foreign host.");
    }

    private void sendFile(String input) throws IOException {
        String[] splittedInput = input.split(" ");
        if (splittedInput.length == 3) {
            String filename = splittedInput[2];
            if (Files.exists(Paths.get(filename))) {
                long fileSize = Files.size(Paths.get(filename));
                if (fileSize > 0) {
                    this.out.println(input + " " + fileSize);
                    for (byte b : Files.readAllBytes(Paths.get(filename))) this.out.write(b);
                }
            } else System.out.println("The file specified does not exist !");
        }
    }

    private String cipheredInput(String input) throws IOException {
        byte[] paddedText = PKCS5.addPKCS5Padding(input.getBytes(), DMS);
        DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(paddedText));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        while (dataInputStream.available() > 0) {
            byte[] buffer = new byte[0xF0];
            dataInputStream.readFully(buffer, 0, buffer.length);
            int apduLength = 6 + buffer.length;
            byte[] apdu = new byte[apduLength];
            apdu[0] = CLA;
            apdu[1] = DES_ENCRYPT.getCode();
            apdu[2] = 0;
            apdu[3] = 0;
            apdu[4] = (byte) buffer.length;

            System.arraycopy(buffer, 0, apdu, 5, buffer.length);

            CommandAPDU cmd = new CommandAPDU(apdu);
            ResponseAPDU resp = sendAPDU(cmd, false, this.cardService);
            ResponseCode responseCode = ResponseCode.fromSW(resp.sw());
            if (responseCode == ResponseCode.OK)
                outputStream.write(resp.data());
            else ResponseAPDUtils.printError(responseCode);
        }
        return new String(Base64.encodeBase64(outputStream.toByteArray()));
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
        try {
            listenNetwork();
        } catch (IOException e) {
            System.out.println("[-] " + e.getMessage());
        }
    }

    private void listenNetwork() throws IOException {
        while (LOOP) {
            if (this.in.hasNextLine()) {
                String serverOutput = this.in.nextLine();
                if (serverOutput.startsWith(SERVER_TAG)) {
                    String[] splittedOutput = serverOutput.split(" ", 3);
                    switch (ServerCommand.fromString(splittedOutput[1])) {
                        case SOCKET_CLOSED -> logout();
                        case ENABLE_FILE_TRANSFER_MODE -> {
                            System.out.println("[+] File Transfer Mode enabled.");
                            String filename = Paths.get(this.in.nextLine().trim()).getFileName().toString();
                            if (Files.exists(Paths.get(DIR_PATH + filename))) {
                                try {
                                    Files.delete(Paths.get(DIR_PATH + filename));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            while (this.in.hasNextLine()) {
                                int input = Integer.parseInt(this.in.nextLine()) & 0xFF;
                                if (input == FTM_ENDER) break;
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
                        }

                        case CHALLENGE -> {
                            String challenge = splittedOutput[2];
                            System.out.println("[+] Got a challenge: " + challenge);

                            byte[] challengeByte = Base64.decodeBase64(challenge.getBytes());
                            CommandAPDU cmdApdu;
                            ResponseAPDU resp;
                            System.out.println("[+] Decrypting challenge with SmartCard..");

                            int apduLength = 6 + challengeByte.length;
                            byte[] apdu = new byte[apduLength];
                            apdu[0] = CLA;
                            apdu[1] = RSA_DECRYPT.getCode();
                            apdu[2] = 0;
                            apdu[3] = 0;
                            apdu[4] = (byte) challengeByte.length;

                            System.arraycopy(challengeByte, 0, apdu, 5, challengeByte.length & 0xFF);
                            cmdApdu = new CommandAPDU(apdu);
                            resp = sendAPDU(cmdApdu, false, this.cardService);
                            ResponseCode responseCode = ResponseCode.fromSW(resp.sw());
                            if (responseCode == ResponseCode.OK) {
                                byte[] unciphered = resp.data();
                                this.out.println(new String(Base64.encodeBase64(unciphered)));
                            } else {
                                ResponseAPDUtils.printError(responseCode);
                                System.exit(1);
                            }
                        }

                        case PUBLIC_KEY -> this.out.println(
                                new String(Base64.encodeBase64(this.modulus)) + " " +
                                new String(Base64.encodeBase64(this.publicExponent))
                        );
                        case AUTHENTICATED -> this.isConnected = true;
                        default -> System.out.println(serverOutput);
                    }
                } else if (serverOutput.startsWith("[")) {
                    String[] splittedMessage = serverOutput.split(" ");
                    DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(Base64.decodeBase64(splittedMessage[1].getBytes())));
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    try {
                        while (dataInputStream.available() > 0) {
                            byte[] buffer = new byte[0xF0];
                            dataInputStream.readFully(buffer, 0, buffer.length);

                            int apduLength = 6 + buffer.length;
                            byte[] apdu = new byte[apduLength];
                            apdu[0] = CLA;
                            apdu[1] = DES_DECRYPT.getCode();
                            apdu[2] = 0;
                            apdu[3] = 0;
                            apdu[4] = (byte) buffer.length;

                            System.arraycopy(buffer, 0, apdu, 5, buffer.length);

                            CommandAPDU cmd = new CommandAPDU(apdu);
                            ResponseAPDU resp = sendAPDU(cmd, false, this.cardService);
                            ResponseCode responseCode = ResponseCode.fromSW(resp.sw());
                            if (responseCode == ResponseCode.OK) {
                                byte[] data = resp.data();
                                if (!(dataInputStream.available() > 0))
                                    data = PKCS5.trimPKCS5Padding(data);
                                outputStream.write(data);
                            } else ResponseAPDUtils.printError(responseCode);
                        }
                        splittedMessage[1] = outputStream.toString();
                        System.out.println(String.join(" ", splittedMessage));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (serverOutput.startsWith(ADMIN_TAG)) System.out.println(serverOutput);
            }
        }
    }
}
