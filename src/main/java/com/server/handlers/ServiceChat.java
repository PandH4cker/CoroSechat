package main.java.com.server.handlers;

import main.java.com.server.models.UserModel;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ServiceChat implements Runnable {
    private String pseudo;
    private Socket socket;
    private Scanner in;
    private PrintWriter out;

    private static final Map<String, PrintWriter> users = new HashMap<>();

    /*private static final Set<UserModel> userDB = new HashSet<>() {{
        add(new UserModel("Raphael", "pass12345"));
        add(new UserModel("Thierry", "azerty12345"));
    }};*/

    private static final Set<UserModel> userDB = new HashSet<>();

    public ServiceChat(final Socket socket) {
        this.socket = socket;
    }

    private UserModel getUser(String username) {
        for (UserModel user : userDB)
            if (user.getUsername().equals(username)) return user;
        return null;
    }

    @Override
    public void run() {
        try {
            this.in = new Scanner(this.socket.getInputStream(), StandardCharsets.UTF_8);
            this.in.useLocale(new Locale("fr", "FR"));
            this.out = new PrintWriter(
                    new OutputStreamWriter(this.socket.getOutputStream(), StandardCharsets.UTF_8),
                    true
            );

            if (this.in.hasNextLine()) {
                do {
                    String username = null, password = null;
                    do {
                        this.out.println("<SYSTEM> username: ");
                        if (this.in.hasNextLine())
                            username = this.in.nextLine().trim();
                    } while(Objects.requireNonNull(username).isEmpty());

                    UserModel user = getUser(username);

                    do {
                        this.out.println("<SYSTEM> password: ");
                        if (this.in.hasNextLine())
                            password = this.in.nextLine();
                    } while(Objects.requireNonNull(password).isEmpty());

                    //UserModel user = new UserModel(username, password);
                    if (user != null) {
                        if (user.getPassword().equals(password)) {
                            if (users.containsKey(username)) {
                                this.out.println("<SYSTEM> Error: An user with the same pseudo is already connected.");
                                this.socket.close();
                                return;
                            }
                            this.pseudo = user.getUsername();
                        } else this.out.println("Wrong username or password!");
                    } else {
                        userDB.add(new UserModel(username, password));
                        this.out.println("<SYSTEM> New user created!");
                        this.pseudo = username;
                    }
                } while (users.containsKey(this.pseudo) || this.pseudo == null ||this.pseudo.isEmpty());
            }

            for (PrintWriter writer : users.values())
                writer.println("<SYSTEM> " + this.pseudo + " has joined the chat.");
            for (String pseudo : users.keySet())
                this.out.println("<SYSTEM> " + pseudo + " has joined the chat.");

            users.put(this.pseudo, this.out);

            //ServerChat.getWriters().add(this.out);

            while (true) {
                if (this.in.hasNextLine()) {
                    String input = this.in.nextLine();
                    System.out.println(input);
                    if (input.toLowerCase().startsWith("/logout") || input.toLowerCase().startsWith("/exit")) {
                        users.remove(this.pseudo);
                        this.socket.close();

                        for (PrintWriter writer : users.values())
                            writer.println("<SYSTEM> " + this.pseudo + " has disconnected");
                        return;
                    } else if (input.toLowerCase().startsWith("/list")) {
                        this.out.println("<SYSTEM> List of connected users: ");
                        for (String pseudo : users.keySet())
                            this.out.println("<SYSTEM> " + pseudo);
                    } else if (input.toLowerCase().startsWith("/msg")) {
                        String[] splittedInput = input.split(" ");
                        if (splittedInput.length < 3)
                            this.out.println("<SYSTEM> Usage: /msg username message");
                        else {
                            String pseudo = splittedInput[1];
                            String message = String.join(
                                    " ",
                                    Arrays.copyOfRange(splittedInput, 2, splittedInput.length)
                            );
                            if(users.containsKey(pseudo)) {
                                users.get(pseudo).println("[" + this.pseudo + "] " + message);
                            } else {
                                this.out.println("<SYSTEM> " + pseudo + " is not connected");
                            }
                        }
                    }
                    else if (!input.trim().isEmpty()) {
                        for (PrintWriter writer : users.values())
                            writer.println("[" + this.pseudo +  "] " + input);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
