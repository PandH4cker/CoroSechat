package main.java.com.server.handlers;

public enum ServerCommand {
    LOGOUT("/logout"),
    EXIT("/exit"),
    LIST("/list"),
    PRIVATE_MESSAGE("/msg"),
    KILL("/kill"),
    KILALL("/killall"),
    HALT("/halt"),
    DELETE_ACCOUNT("/deleteAccount"),
    ADD_ACCOUNT("/addAccount"),
    LOAD_BDD("/loadBDD"),
    SAVE_BDD("/saveBDD"),
    UNKNOWN("");

    private String command;

    ServerCommand(final String command) {
        this.command = command;
    }

    @Override
    public String toString() {
        return this.command;
    }

    public static ServerCommand fromString(final String s) {
        for (ServerCommand cmd : ServerCommand.values()) {
            if (s.startsWith(cmd.toString())) return cmd;
        } return UNKNOWN;
    }
}
