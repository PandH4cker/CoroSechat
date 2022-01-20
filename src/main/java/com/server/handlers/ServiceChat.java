package main.java.com.server.handlers;

import main.java.com.logger.Logger;
import main.java.com.logger.LoggerFactory;
import main.java.com.logger.level.Level;
import main.java.com.server.models.Client;
import main.java.com.server.models.UserModel;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ServiceChat implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ServiceChat.class.getSimpleName());

    private String pseudo;
    private Socket socket;
    private Scanner in;
    private PrintWriter out;
    private boolean isAdmin;

    private static final Map<String, Client> users = new HashMap<>();

    /*private static final Set<UserModel> userDB = new HashSet<>() {{
        add(new UserModel("Raphael", "pass12345"));
        add(new UserModel("Thierry", "azerty12345"));
    }};*/

    private static final Set<UserModel> userDB = new HashSet<>();

    public ServiceChat(final Socket socket) throws IOException {
        this.socket = socket;
        this.in = new Scanner(this.socket.getInputStream(), StandardCharsets.UTF_8);
        this.in.useLocale(new Locale("fr", "FR"));
        this.out = new PrintWriter(
                new OutputStreamWriter(this.socket.getOutputStream(), StandardCharsets.UTF_8),
                true
        );
        this.isAdmin = false;
    }

    public ServiceChat() {
        this.in = new Scanner(System.in, StandardCharsets.UTF_8);
        this.in.useLocale(new Locale("fr", "FR"));
        this.out = new PrintWriter(
                new OutputStreamWriter(System.out, StandardCharsets.UTF_8),
                true
        );
        this.isAdmin = true;
        this.pseudo = "<ADMIN>";
    }

    private UserModel getUser(String username) {
        for (UserModel user : userDB)
            if (user.getUsername().equals(username)) return user;
        return null;
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

                if (this.in.hasNextLine()) {
                    do {
                        String username = null, password = null;
                        do {
                            if (this.in.hasNextLine())
                                username = this.in.nextLine().trim();
                            if (Objects.requireNonNull(username).isEmpty())
                                Writifier.systemWriter(this.out, "username: ");
                        } while(Objects.requireNonNull(username).isEmpty());

                        UserModel user = getUser(username);

                        do {
                            Writifier.systemWriter(this.out, "password: ");
                            if (this.in.hasNextLine())
                                password = this.in.nextLine();
                        } while(Objects.requireNonNull(password).isEmpty());

                        //UserModel user = new UserModel(username, password);
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
                            userDB.add(new UserModel(username, password, 1));
                            Writifier.systemWriter(this.out,"New user created!");
                            logger.log("A new user has been created with username " + username, Level.INFO);
                            this.socket.close();
                            return;
                        }
                    } while (users.containsKey(this.pseudo) || this.pseudo == null ||this.pseudo.isEmpty());
                }
            }

            if (!this.isAdmin)
                for (Client client : users.values())
                    Writifier.systemWriter(client.getWriter(),"" + this.pseudo + " has joined the chat.");
            for (String pseudo : users.keySet())
                Writifier.systemWriter(this.out,"" + pseudo + " has joined the chat.");

            if (!this.isAdmin)
                users.put(this.pseudo, new Client(this.socket));

            while (true) {
                if (this.in.hasNextLine()) {
                    String input = this.in.nextLine();
                    if (!input.trim().isEmpty()) {
                        ServerCommand command = ServerCommand.fromString(input);
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
                                   if (splittedInput.length < 2)
                                       Writifier.systemWriter(this.out, "Usage: /kill username");
                                   else {
                                       String username = splittedInput[1];
                                       if (users.containsKey(username)) {
                                           users.get(username).getSocket().close();
                                           users.remove(username);
                                           for (Client client : users.values())
                                               Writifier.systemWriter(client.getWriter(), username + " has been killed by the administrator.");
                                           logger.log(username + " has been killed by the administrator.", Level.INFO);
                                       } else Writifier.systemWriter(this.out,pseudo + " is not connected");
                                   }
                               }
                            }
                            case LIST -> listUsers();
                            case PRIVATE_MESSAGE -> privateMessage(input, this.isAdmin);
                            default -> broadcastMessage(input, this.isAdmin);
                        }
                    }
                    if (!this.isAdmin) logger.log("[" + this.pseudo + "] " + input, Level.INFO);
                    else logger.log(this.pseudo + " " + input, Level.INFO);
                }
            }
        } catch (IOException e) {
            logger.log(e.getMessage(), Level.ERROR);
        }
    }

    private void broadcastMessage(String input, boolean isAdmin) {
        for (Client client : users.values()) Writifier.messageWriter(client.getWriter(), this.pseudo, input, isAdmin);
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
            if(users.containsKey(pseudo))
                Writifier.messageWriter(users.get(pseudo).getWriter(), this.pseudo, message, isAdmin);
            else
                Writifier.systemWriter(this.out,pseudo + " is not connected");
        }
    }

    private void listUsers() {
        Writifier.systemWriter(this.out,"List of connected users: ");
        for (String pseudo : users.keySet())
            Writifier.systemWriter(this.out, pseudo);
    }

    private void logout() throws IOException {
        users.remove(this.pseudo);
        this.socket.close();

        for (Client client : users.values())
           Writifier.systemWriter(client.getWriter(), this.pseudo + " has disconnected");
    }
}
