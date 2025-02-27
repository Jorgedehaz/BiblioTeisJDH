package com.example.biblioteca.API.models;

import java.util.List;

public class UserSingelton {
    private static UserSingelton instance;
    private User user;

    private UserSingelton() {
        this.user = new User();
    }

    public static UserSingelton getInstance() {
        if(instance == null){
            instance = new UserSingelton();
        }
        return instance;
    }

    public static void setInstance(UserSingelton instance) {
        UserSingelton.instance = instance;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
