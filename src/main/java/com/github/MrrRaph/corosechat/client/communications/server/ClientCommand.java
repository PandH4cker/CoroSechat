package com.github.MrrRaph.corosechat.client.communications.server;

public enum ClientCommand {
    LOGOUT("/logout"),
    EXIT("/exit"),
    LIST("/list"),
    PRIVATE_MESSAGE("/msg"),
    SEND_FILE("/sendFile"),
    HELP("/help"),
    UNKNOWN("");

    private String command;

    ClientCommand(final String command) {
        this.command = command;
    }

    @Override
    public String toString() {
        return this.command;
    }

    public static ClientCommand fromString(final String s) {
        for (ClientCommand cmd : ClientCommand.values()) if (s.equalsIgnoreCase(cmd.toString())) return cmd;
        return UNKNOWN;
    }
}
