package com.github.MrrRaph.corosechat.server.models;

import java.security.PublicKey;
import java.util.Objects;

public class User {
    private String username;
    private PublicKey publicKey;
    private UserGroup group;

    public User(final String username, final PublicKey publicKey, final UserGroup group) {
        this.username = username;
        this.publicKey = publicKey;
        this.group = group;
    }

    public User() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public UserGroup getGroup() {
        return group;
    }

    public void setGroup(UserGroup group) {
        this.group = group;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return username.equals(user.username) && publicKey.equals(user.publicKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, publicKey);
    }
}
