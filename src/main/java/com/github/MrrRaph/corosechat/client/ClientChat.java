package com.github.MrrRaph.corosechat.client;

import com.github.MrrRaph.corosechat.client.exceptions.NoKeysGeneratedException;
import com.github.MrrRaph.corosechat.client.utils.card.ResponseAPDUtils;
import com.github.MrrRaph.corosechat.client.card.codes.ResponseCode;
import opencard.core.service.CardRequest;
import opencard.core.service.SmartCard;
import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.CommandAPDU;
import opencard.core.terminal.ResponseAPDU;
import opencard.opt.util.PassThruCardService;
import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

import static com.github.MrrRaph.corosechat.client.utils.bytes.ByteUtil.printBytes;
import static com.github.MrrRaph.corosechat.client.utils.card.CardUtils.*;
import static com.github.MrrRaph.corosechat.client.card.codes.CommandCode.*;
import static java.nio.file.StandardOpenOption.*;

public class ClientChat extends Thread {
    private final static String DIR_PATH= "download/";

    private Socket socket;
    private PrintWriter out;
    private Scanner in;
    private PassThruCardService cardService;

    private static boolean LOOP = true;

    private byte[] modulus;
    private byte[] publicExponent;

    public ClientChat(String[] args) throws CardTerminalException {
        try {
            SmartCard.start();
            System.out.print("Smartcard inserted?... ");

            CardRequest cr = new CardRequest(CardRequest.ANYCARD, null, null);

            SmartCard sm = SmartCard.waitForCard(cr);

            if (sm != null)
                System.out.println("got a SmartCard object!\n");
            else
                System.out.println("did not get a SmartCard object!\n");

            this.cardService = initNewCard(sm);

        } catch(Exception e) {
            System.out.println("TheClient error: " + e.getMessage());
        }

        getPublicRSAKey();

        this.initStreams(args);
        this.start();
        this.listenConsole();
        SmartCard.shutdown();
    }

    private void getPublicRSAKey() {
        try {
            this.modulus = getPublicRSAModulus();
            this.publicExponent = getPublicRSAExponent();
        } catch (NoKeysGeneratedException e) {
            generateRSAKeyPair();
        }
    }

    private byte[] getPublicRSAExponent() throws NoKeysGeneratedException {
        ResponseAPDU resp;
        CommandAPDU cmd;
        ResponseCode responseCode;
        cmd = new CommandAPDU(new byte[]{
                CLA,
                GET_PUBLIC_RSA_KEY.getCode(),
                (byte) 0x00,
                GET_EXPONENT,
                (byte) 0x00
        });
        resp = sendAPDU(cmd, this.cardService);
        responseCode = ResponseAPDUtils.byteArrayToResponseCode(resp.getBytes());

        if (responseCode == ResponseCode.OK) {
            byte[] data = resp.data();
            byte[] publicExponent = new byte[data[0] & 0xFF];

            System.arraycopy(data, 1, publicExponent, 0, data[0] & 0xFF);
            System.out.println("[+] Retrieved Public Exponent: " + printBytes(publicExponent));

            return publicExponent;
        } else {
            ResponseAPDUtils.printError(responseCode);
            throw new NoKeysGeneratedException("Exponent not found in the card.");
        }
    }

    private byte[] getPublicRSAModulus() throws NoKeysGeneratedException {
        CommandAPDU cmd = new CommandAPDU(new byte[]{
                CLA,
                GET_PUBLIC_RSA_KEY.getCode(),
                (byte) 0x00,
                GET_MODULUS,
                (byte) 0x00
        });
        ResponseAPDU resp = sendAPDU(cmd, this.cardService);
        ResponseCode responseCode = ResponseAPDUtils.byteArrayToResponseCode(resp.getBytes());
        if (responseCode == ResponseCode.OK) {
            byte[] data = resp.data();
            byte[] modulus = new byte[data[0] & 0xFF];

            System.arraycopy(data, 1, modulus, 0, data[0] & 0xFF);
            System.out.println("[+] Retrieved Modulus: " + printBytes(modulus));
            return modulus;
        } else {
            ResponseAPDUtils.printError(responseCode);
            throw new NoKeysGeneratedException("Modulus not found in the card.");
        }
    }

    private void generateRSAKeyPair() {
        CommandAPDU cmd;
        ResponseAPDU resp;
        System.out.println("Generating RSA Key Pair..");

        cmd = new CommandAPDU(new byte[] { (byte)0x90,  (byte)0xF6, (byte)0x00, (byte)0x00, (byte)0x00 });
        resp = sendAPDU(cmd, this.cardService);
        ResponseCode responseCode = ResponseAPDUtils.byteArrayToResponseCode(resp.getBytes());
        if (responseCode == ResponseCode.OK) {
            System.out.println("[+] Successfully Generated RSA Key Pair !");
            getPublicRSAKey();
        } else {
            ResponseAPDUtils.printError(responseCode);
            System.exit(1);
        }
    }

    public static void main(String[] args) throws CardTerminalException {
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
                } else {
                    this.out.println(input);
                }
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
                        CommandAPDU cmdApdu;
                        ResponseAPDU resp;
                        System.out.println("Decrypting challenge with SmartCard..");

                        int apduLength = 6 + challengeByte.length;
                        byte[] apdu = new byte[apduLength];
                        apdu[0] = CLA;
                        apdu[1] = RSA_DECRYPT.getCode();
                        apdu[2] = 0;
                        apdu[3] = 0;
                        apdu[4] = (byte) challengeByte.length;

                        System.arraycopy(challengeByte, 0, apdu, 5, challengeByte.length & 0xFF);
                        cmdApdu = new CommandAPDU(apdu);
                        resp = sendAPDU(cmdApdu, this.cardService);
                        ResponseCode responseCode = ResponseAPDUtils.byteArrayToResponseCode(resp.getBytes());
                        if (responseCode == ResponseCode.OK) {
                            byte[] unciphered = resp.data();
                            System.out.println("Retrieved Challenge: " + printBytes(challengeByte));

                            this.out.println(new String(Base64.encodeBase64(unciphered)));
                        } else {
                            ResponseAPDUtils.printError(responseCode);
                            System.exit(1);
                        }
                    } else if (serverOutput.startsWith("<SYSTEM> Public Key:")) {
                        this.out.println(
                                new String(Base64.encodeBase64(this.modulus)) +
                                        " " +
                                        new String(Base64.encodeBase64(this.publicExponent))
                        );
                    }
                }
            }
        }
    }
}
