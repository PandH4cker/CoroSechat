package main.java.com.server.models;

import java.util.Objects;

public class UserModel {
    private String username;
    private String password;
    private int group;

    public UserModel(final String username, final String password, final int group) {
        this.username = username;
        this.password = password;
        this.group = group;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserModel userModel = (UserModel) o;
        return username.equals(userModel.username) && password.equals(userModel.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password);
    }
}
