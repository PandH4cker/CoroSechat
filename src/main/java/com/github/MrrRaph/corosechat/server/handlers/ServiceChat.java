package com.github.MrrRaph.corosechat.server.handlers;

import com.github.MrrRaph.corosechat.logger.Logger;
import com.github.MrrRaph.corosechat.logger.LoggerFactory;
import com.github.MrrRaph.corosechat.logger.level.Level;
import com.github.MrrRaph.corosechat.server.communications.ServerCommand;
import com.github.MrrRaph.corosechat.server.models.Client;
import com.github.MrrRaph.corosechat.server.models.User;
import com.github.MrrRaph.corosechat.server.models.UserGroup;
import com.github.MrrRaph.corosechat.server.utils.StringUtils;
import com.github.MrrRaph.corosechat.server.utils.Writifier;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jetbrains.annotations.NotNull;

import javax.crypto.*;
import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.*;
import java.util.stream.Stream;

import static com.github.MrrRaph.corosechat.server.ServerChat.*;
import static com.github.MrrRaph.corosechat.server.communications.ClientCommand.*;

public class ServiceChat implements Runnable {
    private static final Logger logger      = LoggerFactory.getLogger(ServiceChat.class.getSimpleName());
    private static final int CHALLENGE_SIZE = 128;
    private static final short FTM_ENDER    = 0xFF;
    private static final String ADMIN_TAG   = "<ADMIN>";
    private static final Map<String, Client> users = new HashMap<>();
    private static final Set<User> userDB = new HashSet<>();

    private String pseudo;
    private Socket socket;
    private final Scanner in;
    private final PrintWriter out;
    private final boolean isAdmin;
    private Cipher cRSA_NO_PAD;

    public ServiceChat(final Socket socket) throws IOException {
        this.socket = socket;
        this.in = new Scanner(this.socket.getInputStream());
        this.out = new PrintWriter(this.socket.getOutputStream(), true);
        this.isAdmin = false;

        Security.addProvider(new BouncyCastleProvider());
        try {
            this.cRSA_NO_PAD = Cipher.getInstance("RSA/NONE/NoPadding", BouncyCastleProvider.PROVIDER_NAME);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException e) {
            logger.log(e.getMessage(), Level.ERROR);
        }
    }

    public ServiceChat() {
        this.in = new Scanner(System.in);
        this.out = new PrintWriter(System.out, true);
        this.isAdmin = true;
        this.pseudo = ADMIN_TAG;
    }

    private User getUser(String username) {
        for (User user : userDB)
            if (user.getUsername().equals(username)) return user;
        return null;
    }

    private void deleteUser(String username) {
        userDB.removeIf(user -> user.getUsername().equals(username));
    }

    @Override
    public void run() {
        try {
            if (users.size() + 1 > MAX_USERS) {
                closeSocket(this.out, this.socket);
                return;
            }

            if (!this.isAdmin) {
                logger.log("A new user has initiated a connection on IP " + this.socket.getInetAddress().getHostAddress(), Level.INFO);

                try {
                    String username = askUsername();
                    User user = getUser(username);

                    if (user != null) {
                        byte[] challengeBytes = generateChallenge(CHALLENGE_SIZE);

                        this.cRSA_NO_PAD.init(Cipher.ENCRYPT_MODE, user.getPublicKey());
                        byte[] ciphered = cRSA_NO_PAD.doFinal(challengeBytes);

                        if (!isAuthenticated(username, user, challengeBytes, ciphered)) return;
                    } else {
                        Writifier.systemWriter(this.out, PUBLIC_KEY);
                        String[] splittedResponse = new String[0];
                        do {
                            if (this.in.hasNextLine())
                                splittedResponse = this.in.nextLine().split(" ");
                        } while (splittedResponse.length == 0);

                        PublicKey publicKey = getPublicKey(splittedResponse[0].getBytes(), splittedResponse[1].getBytes());
                        userDB.add(new User(username, publicKey, UserGroup.REGULAR_USER));
                        Writifier.systemWriter(this.out,"A new user has been created! Connect back again to login.");
                        Writifier.systemWriter(this.out, SOCKET_CLOSED);
                        logger.log("A new user has been created with username " + username, Level.INFO);
                        closeSocket(this.out, this.socket);
                        return;
                    }
                } catch (NoSuchAlgorithmException  | InvalidKeyException |
                         IllegalBlockSizeException | BadPaddingException e) {
                    logger.log(e.getMessage(), Level.ERROR);
                }
            }

            if (!this.isAdmin)
                for (Client client : users.values())
                    Writifier.systemWriter(client.getWriter(),"" + this.pseudo + " has joined the chat.");
            for (String pseudo : users.keySet())
                Writifier.systemWriter(this.out,"" + pseudo + " has joined the chat.");

            if (!this.isAdmin)
                users.put(this.pseudo, new Client(this.socket));

            if (!this.isAdmin) Writifier.systemWriter(this.out, "### Welcome to CoroSeChat ###");

            while (this.in.hasNextLine()) {
                String input = StringUtils.removeNonPrintable(this.in.nextLine());
                if (!input.trim().isEmpty()) {
                    ServerCommand command = ServerCommand.fromString(input.split(" ")[0]);
                    switch (command) {
                        case LOGOUT, EXIT -> {
                            if (!this.isAdmin) {
                                logout();
                                logger.log(this.pseudo + " has disconnected [" + this.socket.getInetAddress().getHostAddress() + "]", Level.INFO);
                                return;
                            } else Writifier.systemWriter(this.out, "You cannot disconnect from admin account !");
                        }

                        case KILL -> {
                           if (isAdmin) {
                               String[] splittedInput = input.split(" ");
                               if (splittedInput.length < 2) Writifier.systemWriter(this.out, "Usage: /kill username");
                               else killUser(splittedInput[1]);
                           }
                        }

                        case KILLALL -> {
                            if (isAdmin) killall();
                        }

                        case HALT -> {
                            if (isAdmin) {
                                for (Client client : users.values()) Writifier.systemWriter(client.getWriter(), SOCKET_CLOSED);
                                System.exit(0);
                            }
                        }

                        case SEND_FILE -> {
                            String[] splittedInput = input.split(" ");
                            if (splittedInput.length < 3) Writifier.systemWriter(this.out, "Usage: /sendFile username filename");
                            else {
                                if (users.containsKey(splittedInput[1])) {
                                    Writifier.systemWriter(this.out, "Sending File: " + splittedInput[2]);

                                    Writifier.systemWriter(users.get(splittedInput[1]).getWriter(), ENABLE_FILE_TRANSFER_MODE);
                                    this.out.println(splittedInput[2]);

                                    BufferedInputStream bufferedInputStream = new BufferedInputStream(this.socket.getInputStream());
                                    for (long i = 0; i < Long.parseLong(splittedInput[3]); ++i) {
                                        int b = bufferedInputStream.read();
                                        //System.out.print((char) b);
                                        users.get(splittedInput[1])
                                                .getWriter()
                                                .println(b);
                                    }
                                    users.get(splittedInput[1]).getWriter().println(FTM_ENDER);
                                } else Writifier.systemWriter(this.out, "User " + splittedInput[2] + " is not connected.");
                            }
                        }

                        case DELETE_ACCOUNT -> {
                            if (isAdmin) {
                                String[] splittedInput = input.split(" ");
                                if (splittedInput.length < 2) Writifier.systemWriter(this.out, "Usage: /deleteAccount username");
                                else deleteAccount(splittedInput[1]);
                            }
                        }

                        case ADD_ACCOUNT -> {
                            if (isAdmin) {
                                String[] splittedInput = input.split(" ");
                                if (splittedInput.length < 3) Writifier.systemWriter(this.out, "Usage: /addAccount username publicKeyModulus publicKeyExponent");
                                else {
                                    PublicKey publicKey = getPublicKey(splittedInput[2].getBytes(), splittedInput[3].getBytes());
                                    addAccount(splittedInput[1], publicKey);
                                }
                            }
                        }

                        case LOAD_DATABASE -> {
                            if (isAdmin) {
                                logger.log("Loading database..", Level.INFO);
                                loadDatabase();
                            }
                        }

                        case SAVE_DATABASE -> {
                            if (isAdmin) {
                                logger.log("Saving database..", Level.INFO);
                                saveDatabase();
                            }
                        }
                        case LIST -> listUsers();
                        case PRIVATE_MESSAGE -> privateMessage(input, this.isAdmin);
                        case HELP -> {
                            String help = """
                                    ## Regular User Commands:
                                    <SYSTEM> /logout|/exit\t\t\t\t-- Disconnect / Quit
                                    <SYSTEM> /list\t\t\t\t\t-- List current connected users
                                    <SYSTEM> /msg username message\t\t\t-- Send a Private Message to username
                                    <SYSTEM> /sendfile username filename\t\t-- Send a File to username
                                    <SYSTEM> /help\t\t\t\t\t-- Show this help message
                                    """;

                            if (this.isAdmin) help += """
                                    ## Admin Commands:
                                    /kill username\t\t-- Kill a connected user
                                    /killall\t\t-- Kill all connected users
                                    /halt\t\t-- Stop the server
                                    /deleteAccount username\t\t-- Delete an existing account
                                    /addAccount username password\t\t-- Add an account
                                    /loadBDD\t\t-- Load the Database
                                    /saveBDD\t\t-- Save the Database
                                    """;

                            Writifier.systemWriter(this.out, help);
                        }
                        default -> broadcastMessage(input, this.isAdmin);
                    }
                    if (!this.isAdmin) logger.log("[" + this.pseudo + "] " + input, Level.INFO);
                    else logger.log(this.pseudo + " " + input, Level.INFO);
                }
            }
            logout();
        } catch (IOException | InterruptedException e) {
            logger.log(e.getMessage(), Level.ERROR);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    private PublicKey getPublicKey(byte[] modulusBytes, byte[] publicExponent) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String mod_s = new String(Hex.encodeHex(Base64.decodeBase64(modulusBytes)));
        String pub_s = new String(Hex.encodeHex(Base64.decodeBase64(publicExponent)));

        BigInteger modulus = new BigInteger(mod_s, 16);
        BigInteger pubExponent = new BigInteger(pub_s, 16);

        RSAPublicKeySpec publicSpec = new RSAPublicKeySpec(modulus, pubExponent);

        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePublic(publicSpec);
    }

    private boolean isAuthenticated(String username, User user, byte[] challengeBytes, byte[] ciphered) throws IOException {
        Writifier.systemWriter(this.out, CHALLENGE + " " + new String(Base64.encodeBase64(ciphered)));

        String uncipheredChallenge = "";
        do {
            if (this.in.hasNextLine()) uncipheredChallenge = this.in.nextLine();
        } while(uncipheredChallenge.isEmpty());

        byte[] unciphered =  Base64.decodeBase64(uncipheredChallenge.getBytes());
        if (Arrays.equals(unciphered, challengeBytes)) {
            if (users.containsKey(username)) {
                Writifier.systemWriter(this.out, "Error: An user with the same pseudo is already connected.");
                logger.log("Failing attempt for connecting to " + username + ": User already connected.", Level.WARNING);
                closeSocket(this.out, this.socket);
                return false;
            }

            Writifier.systemWriter(this.out, "Welcome ! You are now authenticated.");
            Writifier.systemWriter(this.out, AUTHENTICATED);
            this.pseudo = user.getUsername();
        } else {
            Writifier.systemWriter(this.out, "Challenge verification failed.");
            closeSocket(this.out, this.socket);
            return false;
        }
        return true;
    }

    private void closeSocket(PrintWriter out, Socket socket) throws IOException {
        Writifier.systemWriter(out, SOCKET_CLOSED);
        socket.close();
    }

    @NotNull
    private byte[] generateChallenge(int challengeSize) {
        byte[] challengeBytes = new byte[challengeSize];
        Random r = new Random(new Date().getTime());
        r.nextBytes(challengeBytes);
        challengeBytes[0] &= 0x3F;
        return challengeBytes;
    }

    @NotNull
    private String askUsername() {
        String username = null;
        do {
            Writifier.systemWriter(this.out, "username: ");
            if (this.in.hasNextLine())
                username = StringUtils.removeNonPrintable(this.in.nextLine().trim());
        } while(Objects.requireNonNull(username).trim().isEmpty());
        return username;
    }

    private void saveDatabase() throws IOException {
        Path p = Paths.get("files/user_bdd.txt");
        Files.createDirectories(p);
        if (Files.exists(p))
            Files.delete(p);

        for (User u : userDB)
            Files.write(p, (
                    u.getUsername() + ":" +
                    new String(Base64.encodeBase64(((RSAPublicKey) u.getPublicKey()).getModulus().toByteArray())) + ":" +
                    new String(Base64.encodeBase64(((RSAPublicKey) u.getPublicKey()).getPublicExponent().toByteArray())) + ":" +
                    u.getGroup() + "\n"
            ).getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
    }

    private void loadDatabase() throws IOException {
        Path p = Paths.get("files/user_bdd.txt");
        if (Files.exists(p)) {
            try (Stream<String> stream = Files.lines(p)) {
                stream.forEach(line -> {
                    String[] splittedLine = line.split(":");
                    try {
                        userDB.add(new User(
                                splittedLine[0],
                                getPublicKey(splittedLine[1].getBytes(), splittedLine[2].getBytes()),
                                UserGroup.REGULAR_USER
                        ));
                    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                        e.printStackTrace();
                    }
                    logger.log(splittedLine[0] + " account has been added from database.", Level.INFO);
                });
            }
        }
    }

    private void addAccount(String username, PublicKey publicKey) {
        if (userDB.add(new User(username, publicKey, UserGroup.REGULAR_USER)))
            logger.log(username + " account has been added by the administrator.", Level.INFO);
        else Writifier.systemWriter(this.out, username + " already exists.");
    }

    private void deleteAccount(String username) throws IOException {
        killUser(username);
        deleteUser(username);
        logger.log(username + " account has been deleted by the administrator.", Level.INFO);
    }

    private synchronized void killall() throws IOException {
        Iterator<String> iter = users.keySet().iterator();
        while (iter.hasNext()) {
            String username = iter.next();
            closeSocket(users.get(username).getWriter(), users.get(username).getSocket());
            iter.remove();

            for (Client client : users.values())
                Writifier.systemWriter(client.getWriter(), username + " has been killed by the administrator.");
            logger.log(username + " has been killed by the administrator.", Level.INFO);
        }
    }

    private synchronized void killUser(String username) throws IOException {
        if (users.containsKey(username)) {
            closeSocket(users.get(username).getWriter(), users.get(username).getSocket());
            users.remove(username);

            for (Client client : users.values())
                Writifier.systemWriter(client.getWriter(), username + " has been killed by the administrator.");
            logger.log(username + " has been killed by the administrator.", Level.INFO);
        } else Writifier.systemWriter(this.out,username + " is not connected");
    }

    private synchronized void broadcastMessage(String input, boolean isAdmin) throws InterruptedException {
        for (Client client : users.values()) Writifier.messageWriter(client.getWriter(), this.pseudo, input, isAdmin);
        if (!isAdmin) Thread.sleep(500);
    }

    private void privateMessage(String input, boolean isAdmin) {
        String[] splittedInput = input.split(" ");
        if (splittedInput.length < 3)
            Writifier.systemWriter(this.out,"Usage: /msg username message");
        else {
            String pseudo = splittedInput[1];
            String message = String.join(
                    " ",
                    Arrays.copyOfRange(splittedInput, 2, splittedInput.length)
            );
            synchronized (users) {
                if(users.containsKey(pseudo))
                    Writifier.messageWriter(users.get(pseudo).getWriter(), this.pseudo, message, isAdmin);
                else
                    Writifier.systemWriter(this.out,pseudo + " is not connected");
            }
        }
    }

    private void listUsers() {
        Writifier.systemWriter(this.out,"List of connected users: ");
        synchronized (users) {
            for (String pseudo : users.keySet())
                Writifier.systemWriter(this.out, pseudo);
        }
    }

    private void logout() throws IOException {
        users.remove(this.pseudo);
        this.socket.close();

        synchronized (users) {
            for (Client client : users.values())
                Writifier.systemWriter(client.getWriter(), this.pseudo + " has disconnected");
        }
    }
}
