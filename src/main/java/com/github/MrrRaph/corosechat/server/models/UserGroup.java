package com.github.MrrRaph.corosechat.server.models;

import com.github.MrrRaph.corosechat.server.models.exceptions.UnknownGroupException;

public enum UserGroup {
    ADMIN(0),
    REGULAR_USER(1);

    private int id;

    UserGroup(final int id) {
        this.id = id;
    }

    public static UserGroup fromId(final int id) throws UnknownGroupException{
        for (UserGroup g : UserGroup.values())
            if (g.id == id) return g;
        throw new UnknownGroupException("Unknown group");
    }
}
