package com.github.MrrRaph.corosechat.server.communications;

public enum ClientCommand {
    SOCKET_CLOSED("/socketClosed"),
    ENABLE_FILE_TRANSFER_MODE("/enableFTM"),
    CHALLENGE("/challenge"),
    PUBLIC_KEY("/pk"),
    AUTHENTICATED("/authenticated"),
    UNKNOWN("");

    private final String command;

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
