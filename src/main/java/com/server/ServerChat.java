package main.java.com.server;

import main.java.com.server.handlers.ServiceChat;
import sun.misc.Signal;

import java.io.PrintWriter;
import java.net.PortUnreachableException;
import java.net.ServerSocket;
import java.util.HashSet;
import java.util.Set;

/**
 * <h1>The MultiThreadedServer object</h1>
 * <p>
 *     This class is the launcher of the multi threaded server
 * </p>
 * //TODO Include diagram of MultiThreadedServer
 *
 * @author Raphael Dray
 * @version 0.0.8
 * @since 0.0.1
 * @see Set
 * @see PrintWriter
 * @see ServerSocket
 */
public class ServerChat {
    /**
     * The default port if not specified is 8080
     */
    private static final int DEFAULT_PORT = 8080;
    private int port;
    private String host;
    /*private static Set<String> pseudos = new HashSet<>();
    private static Set<PrintWriter> writers = new HashSet<>();*/

    /**
     * Constructor of the multi threaded server.
     * Initialize the host and the port.
     * @param host The host of the server
     * @param port The port of the server
     */
    public ServerChat(final String host, final int port) {
        this.host = host;
        try {
            if (port > 65536) {
                this.port = DEFAULT_PORT;
                throw new PortUnreachableException("Port higher than 65536");
            }
            this.port = port;
            System.out.println("Listening on port " + this.port);
        } catch (PortUnreachableException e) {
            System.err.println(e.getMessage());
        }
    }

    private static void handleSignal(Signal signal) {
        System.out.println("Server exiting..");
        System.exit(0);
    }

    /**
     * The actual port of the server
     * @return int - The port of the server
     */
    public int actualPort() {
        return this.port;
    }

    /**
     * Getter of the pseudos
     * @return Set<String> - The pseudos of the users
     * @see Set<String>
     */
    /*public static Set<String> getPseudos() {
        return pseudos;
    }*/

    /**
     * Getter of the writers
     * @return Set<PrintWriter> - The writers of the users
     * @see Set<PrintWriter>
     */
    /*public static Set<PrintWriter> getWriters() {
        return writers;
    }*/

    public static void main(String[] args) throws Exception {
        handleSignals();
        ServerChat server = new ServerChat("localhost", 4444);
        try(ServerSocket listener = new ServerSocket(server.actualPort())) {
            //noinspection InfiniteLoopStatement
            while (true) new Thread(new ServiceChat(listener.accept())).start();
        }
    }

    private static void handleSignals() {
        Signal.handle(new Signal("INT"), ServerChat::handleSignal);
        Signal.handle(new Signal("TERM"), ServerChat::handleSignal);
    }
}