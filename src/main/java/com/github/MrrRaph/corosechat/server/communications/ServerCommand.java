package com.github.MrrRaph.corosechat.server.communications;

public enum ServerCommand {
    LOGOUT("/logout"),
    EXIT("/exit"),
    LIST("/list"),
    PRIVATE_MESSAGE("/msg"),
    KILL("/kill"),
    KILLALL("/killall"),
    HALT("/halt"),
    DELETE_ACCOUNT("/deleteAccount"),
    ADD_ACCOUNT("/addAccount"),
    LOAD_DATABASE("/loadBDD"),
    SAVE_DATABASE("/saveBDD"),
    SEND_FILE("/sendFile"),
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
            if (s.equalsIgnoreCase(cmd.toString())) return cmd;
        } return UNKNOWN;
    }
}
