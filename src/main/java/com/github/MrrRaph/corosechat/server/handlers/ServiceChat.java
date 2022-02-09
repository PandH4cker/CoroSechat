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

import javax.crypto.*;
import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.*;
import java.util.stream.Stream;

public class ServiceChat implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ServiceChat.class.getSimpleName());

    private String pseudo;
    private Socket socket;
    private final Scanner in;
    private final PrintWriter out;
    private final boolean isAdmin;

    private static final Map<String, Client> users = new HashMap<>();

    private static final Set<User> userDB = new HashSet<>();

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

    private static final byte[] PUBLIC_EXPONENT_B = new byte[] { (byte)0x01,(byte)0x00,(byte)0x01 };

    public ServiceChat(final Socket socket) throws IOException {
        this.socket = socket;
        this.in = new Scanner(this.socket.getInputStream());
        this.out = new PrintWriter(this.socket.getOutputStream(), true);
        this.isAdmin = false;
    }

    public ServiceChat() {
        this.in = new Scanner(System.in);
        this.out = new PrintWriter(System.out, true);
        this.isAdmin = true;
        this.pseudo = "<ADMIN>";
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
            if (users.size() + 1 > 3) {
                this.socket.close();
                return;
            }

            if (!this.isAdmin) {
                logger.log("A new user has initiated a connection on IP " + this.socket.getInetAddress().getHostAddress(), Level.INFO);

                Writifier.systemWriter(this.out, "username: ");
                byte[] challengeBytes = new byte[128];
                try {
                    String username = null;
                    do {
                        if (this.in.hasNextLine())
                            username = StringUtils.removeNonPrintable(this.in.nextLine().trim());
                        if (Objects.requireNonNull(username).isEmpty())
                            Writifier.systemWriter(this.out, "username: ");
                    } while(username.trim().isEmpty());

                    this.pseudo = username;

                    String mod_s = new String(Hex.encodeHex(MODULUS_B));
                    String pub_s = new String(Hex.encodeHex(PUBLIC_EXPONENT_B));

                    BigInteger modulus = new BigInteger(mod_s, 16);
                    BigInteger pubExponent = new BigInteger(pub_s, 16);

                    RSAPublicKeySpec publicSpec = new RSAPublicKeySpec(modulus, pubExponent);

                    KeyFactory factory = KeyFactory.getInstance("RSA");
                    PublicKey pub = factory.generatePublic(publicSpec);

                    Random r = new Random(new Date().getTime());
                    r.nextBytes(challengeBytes);

                    Security.addProvider(new BouncyCastleProvider());
                    Cipher cRSA_NO_PAD = Cipher.getInstance("RSA/NONE/NoPadding", "BC");
                    cRSA_NO_PAD.init(Cipher.ENCRYPT_MODE, pub);
                    challengeBytes[0] = 1;
                    byte[] ciphered = cRSA_NO_PAD.doFinal(challengeBytes);

                    Writifier.systemWriter(this.out, "Challenge:" + new String(Base64.encodeBase64(ciphered)));

                    String uncipheredChallenge = "";
                    do {
                        if (this.in.hasNextLine()) uncipheredChallenge = this.in.nextLine();
                    } while(uncipheredChallenge.isEmpty());

                    byte[] unciphered =  Base64.decodeBase64(uncipheredChallenge.getBytes());
                    if (Arrays.equals(unciphered, challengeBytes)) {
                        Writifier.systemWriter(this.out, "Welcome ! You are now authenticated.");
                    } else {
                        Writifier.systemWriter(this.out, "Challenge verification failed.");
                        Writifier.systemWriter(this.out, "Socket closed.");
                        return;
                    }
                } catch (NoSuchAlgorithmException | NoSuchProviderException |
                         NoSuchPaddingException   | InvalidKeySpecException |
                         InvalidKeyException      | IllegalBlockSizeException | BadPaddingException e) {
                    e.printStackTrace();
                }
                /*if (this.in.hasNextLine()) {
                    do {
                        String username = null, password = null;
                        do {
                            if (this.in.hasNextLine())
                                username = StringUtils.removeNonPrintable(this.in.nextLine().trim());
                            if (Objects.requireNonNull(username).isEmpty())
                                Writifier.systemWriter(this.out, "username: ");
                        } while(username.trim().isEmpty());

                        User user = getUser(username);

                        do {
                            Writifier.systemWriter(this.out, "password: ");
                            if (this.in.hasNextLine())
                                password = this.in.nextLine();
                        } while(Objects.requireNonNull(password).isEmpty());

                        if (user != null) {
                            if (user.getPassword().equals(password)) {
                                if (users.containsKey(username)) {
                                    Writifier.systemWriter(this.out, "Error: An user with the same pseudo is already connected.");
                                    logger.log("Failing attempt for connecting to " + username + ": User already connected.", Level.WARNING);
                                    this.socket.close();
                                    return;
                                }
                                this.pseudo = user.getUsername();
                            } else Writifier.systemWriter(this.out,"Wrong username or password!");
                        } else {
                            userDB.add(new User(username, password, UserGroup.REGULAR_USER));
                            Writifier.systemWriter(this.out,"New user created!");
                            logger.log("A new user has been created with username " + username, Level.INFO);
                            this.socket.close();
                            return;
                        }
                    } while (users.containsKey(this.pseudo) || this.pseudo == null ||this.pseudo.isEmpty());
                }*/
            }

            if (!this.isAdmin)
                for (Client client : users.values())
                    Writifier.systemWriter(client.getWriter(),"" + this.pseudo + " has joined the chat.");
            for (String pseudo : users.keySet())
                Writifier.systemWriter(this.out,"" + pseudo + " has joined the chat.");

            if (!this.isAdmin)
                users.put(this.pseudo, new Client(this.socket));

            if (!this.isAdmin) Writifier.systemWriter(this.out, "### Welcome to CoroSeChat ###");

            while (true) {
                if (this.in.hasNextLine()) {
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
                                    for (Client client : users.values()) Writifier.systemWriter(client.getWriter(), "Socket closed.");
                                    System.exit(0);
                                }
                            }

                            case SEND_FILE -> {
                                String[] splittedInput = input.split(" ");
                                if (splittedInput.length < 3) Writifier.systemWriter(this.out, "Usage: /sendFile username filename");
                                else {
                                    if (users.containsKey(splittedInput[1])) {
                                        Writifier.systemWriter(this.out, "Sending File: " + splittedInput[2]);

                                        Writifier.systemWriter(users.get(splittedInput[1]).getWriter(), "/enable ftm");
                                        this.out.println(splittedInput[2]);

                                        BufferedInputStream bufferedInputStream = new BufferedInputStream(this.socket.getInputStream());
                                        for (long i = 0; i < Long.parseLong(splittedInput[3]); ++i) {
                                            int b = bufferedInputStream.read();
                                            System.out.print((char) b);
                                            users.get(splittedInput[1])
                                                    .getWriter()
                                                    .println(b);
                                        }
                                        users.get(splittedInput[1]).getWriter().println(0xFF);
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
                                    if (splittedInput.length < 3) Writifier.systemWriter(this.out, "Usage: /addAccount username password");
                                    else addAccount(splittedInput[1]);
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
                            default -> broadcastMessage(input, this.isAdmin);
                        }
                        if (!this.isAdmin) logger.log("[" + this.pseudo + "] " + input, Level.INFO);
                        else logger.log(this.pseudo + " " + input, Level.INFO);
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            logger.log(e.getMessage(), Level.ERROR);
        }
    }

    private void saveDatabase() throws IOException {
        Path p = Paths.get("files/user_bdd.txt");
        Files.createDirectories(p);
        if (Files.exists(p))
            Files.delete(p);

        for (User u : userDB)
            Files.write(p, (u.getUsername() + ":" + u.getPassword() + ":" + u.getGroup() + "\n").getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
    }

    private void loadDatabase() throws IOException {
        Path p = Paths.get("files/user_bdd.txt");
        if (Files.exists(p)) {
            try (Stream<String> stream = Files.lines(p)) {
                stream.forEach(line -> {
                    String[] splittedLine = line.split(":");
                    userDB.add(new User(splittedLine[0], splittedLine[1], UserGroup.REGULAR_USER));
                    logger.log(splittedLine[0] + " account has been added from database.", Level.INFO);
                });
            }
        }
    }

    private void addAccount(String username) {
        if (userDB.add(new User(username, username, UserGroup.REGULAR_USER)))
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
            Writifier.systemWriter(users.get(username).getWriter(), "Socket closed.");
            users.get(username).getSocket().close();
            iter.remove();

            for (Client client : users.values())
                Writifier.systemWriter(client.getWriter(), username + " has been killed by the administrator.");
            logger.log(username + " has been killed by the administrator.", Level.INFO);
        }
    }

    private synchronized void killUser(String username) throws IOException {
        if (users.containsKey(username)) {
            Writifier.systemWriter(users.get(username).getWriter(), "Socket closed.");
            users.get(username).getSocket().close();
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
