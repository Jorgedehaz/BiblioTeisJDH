package com.example.biblioteca;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.biblioteca.API.models.Book;
import com.example.biblioteca.API.models.User;
import com.example.biblioteca.API.repository.BookRepository;
import com.example.biblioteca.API.repository.UserRepository;
import com.example.biblioteca.SessionManager;

import java.util.List;

public class LoginActivity extends AppCompatActivity {

    Button btnlogin;
    TextView name, password, error;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        btnlogin = findViewById(R.id.btnlogin);
        name = findViewById(R.id.name);
        password = findViewById(R.id.password);
        error = findViewById(R.id.error);

        sessionManager = new SessionManager(this);

        // Si hay usuario guardado en SharedPreferences, lo mandamos directamente al Inicio
        if (sessionManager.getUser() != null) {
            startActivity(new Intent(this, InicioActivity.class));
            finish();
            return;
        }

        btnlogin.setOnClickListener(this::doLogin);
    }

    private void doLogin(View v) {
        UserRepository ur = new UserRepository();

        BookRepository.ApiCallback<List<User>> cb = new BookRepository.ApiCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> result) {
                for (User u : result) {
                    if ((u.getEmail().contentEquals(name.getText().toString())) &&
                            (u.getPasswordHash().contentEquals(password.getText().toString()))) {

                        // Guardamos el usuario en SharedPreferences
                        sessionManager.saveUser(u);

                        Intent intentlogin = new Intent(v.getContext(), InicioActivity.class);
                        startActivity(intentlogin);
                        finish();
                        return;
                    }
                }
                Toast.makeText(LoginActivity.this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Throwable t) {
                error.setText("Error de Conexión");
            }
        };

        ur.getUsers(cb);
    }
}
