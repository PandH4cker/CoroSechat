package com.github.MrrRaph.corosechat.server.models;

import org.javalite.activejdbc.Model;

import java.util.Objects;

public class User {
    private String username;
    private String password;
    private UserGroup group;

    public User(final String username, final String password, final UserGroup group) {
        this.username = username;
        this.password = password;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
        return username.equals(user.username) && password.equals(user.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password);
    }
}
