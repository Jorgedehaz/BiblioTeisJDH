package com.example.biblioteca;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.biblioteca.API.models.User;
import com.google.gson.Gson;

public class SessionManager {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_USER = "currentUser";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Gson gson;

    public SessionManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.editor = sharedPreferences.edit();
        this.gson = new Gson();
    }

    // Guardar usuario en SharedPreferences
    public void saveUser(User user) {
        String userJson = gson.toJson(user);
        editor.putString(KEY_USER, userJson);
        editor.apply();
    }

    // Obtener usuario de SharedPreferences
    public User getUser() {
        String userJson = sharedPreferences.getString(KEY_USER, null);
        if (userJson != null) {
            return gson.fromJson(userJson, User.class);
        }
        return null; // No hay usuario guardado
    }

    // Eliminar usuario (Logout)
    public void logout() {
        editor.remove(KEY_USER);
        editor.apply();
    }
}
