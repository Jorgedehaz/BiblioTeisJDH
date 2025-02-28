package com.example.biblioteca.API.models;

public class UserSingelton {
    private static UserSingelton instance;
    private User user;

    private UserSingelton() { }

    public static UserSingelton getInstance() {
        if (instance == null) {
            instance = new UserSingelton();
        }
        return instance;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void clearUser() {
        this.user = null;
    }
}
