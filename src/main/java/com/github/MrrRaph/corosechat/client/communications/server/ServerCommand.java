package com.github.MrrRaph.corosechat.client.communications.server;

public enum ServerCommand {
    SOCKET_CLOSED("/socketClosed"),
    ENABLE_FILE_TRANSFER_MODE("/enableFTM"),
    CHALLENGE("/challenge"),
    PUBLIC_KEY("/pk"),
    AUTHENTICATED("/authenticated"),
    UNKNOWN("");

    private final String command;

    ServerCommand(final String command) {
        this.command = command;
    }

    @Override
    public String toString() {
        return this.command;
    }

    public static ServerCommand fromString(final String s) {
        for (ServerCommand cmd : ServerCommand.values()) if (s.equalsIgnoreCase(cmd.toString())) return cmd;
        return UNKNOWN;
    }
}
