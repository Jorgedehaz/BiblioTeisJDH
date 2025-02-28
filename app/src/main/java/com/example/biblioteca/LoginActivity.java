package com.example.biblioteca;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.biblioteca.API.models.User;
import com.example.biblioteca.API.models.UserSingelton;
import com.example.biblioteca.API.repository.BookRepository;
import com.example.biblioteca.API.repository.UserRepository;

import java.util.List;


public class LoginActivity extends AppCompatActivity {

    Button btnlogin;
    TextView name, password, error;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;


        });

        btnlogin = findViewById(R.id.btnlogin);
        name = findViewById(R.id.name);
        password = findViewById(R.id.password);
        error = findViewById(R.id.error);

        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLogin(v);
            }
        });

    }



    private void doLogin(View v) {
        //Declaramos User Repository
        UserRepository ur = new UserRepository();

        // Un nuevo Book Repository para usar el ApiCallback que nos permite comunicarnos con la API
        BookRepository.ApiCallback<List<User>> cb = new BookRepository.ApiCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> result) {
                for (User u : result) {
                    if ((u.getEmail().contentEquals(name.getText().toString())) &&
                            (u.getPasswordHash().contentEquals(password.getText().toString()))) {

                        UserSingelton.getInstance().setUser(u); // Guardamos el user en singelton

                        Intent intentlogin = new Intent(v.getContext(), InicioActivity.class);
                        startActivity(intentlogin);
                        finish(); // Evita volver atrás al login
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                error.setText("Error de Conexión");
            }
        };

        ur.getUsers(cb);
    }

}