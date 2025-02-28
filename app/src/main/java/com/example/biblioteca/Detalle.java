package com.example.biblioteca;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.biblioteca.API.models.Book;
import com.example.biblioteca.API.models.BookLendingForm;
import com.example.biblioteca.API.models.UserSingelton;
import com.example.biblioteca.API.repository.BookLendingRepository;
import com.example.biblioteca.API.repository.BookRepository;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Detalle extends AppCompatActivity {
    private TextView txtAutor, txtFecha, txtDescripcion;
    private ImageView imgDetalle;
    private CheckBox checkDisponible;
    private Button btnPrestar, btnDevolver, btnVolver;
    private BookRepository bookRepository;
    private BookLendingRepository lendingRepository;
    private int bookId;
    private Book book;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle);

        UserSingelton.getInstance().setUser(UserSingelton.getInstance().getUser());

        if (UserSingelton.getInstance().getUser() == null) {
            Log.e("DETALLE", "No hay usuario en Singleton. Redirigiendo al Login.");
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }


        txtAutor = findViewById(R.id.txtAutor);
        txtFecha = findViewById(R.id.txtFecha);
        txtDescripcion = findViewById(R.id.txtdescripcion);
        imgDetalle = findViewById(R.id.imgdetalle);
        checkDisponible = findViewById(R.id.checkDisponible);
        btnPrestar = findViewById(R.id.btnPrestar);
        btnDevolver = findViewById(R.id.btnDevolver);
        btnVolver = findViewById(R.id.btnVolver);

        bookRepository = new BookRepository();
        lendingRepository = new BookLendingRepository();

        bookId = getIntent().getIntExtra("bookId", -1);

        if (bookId != -1) {
            loadBookDetails(bookId);
        }

        btnPrestar.setOnClickListener(v -> prestarLibro());
        btnDevolver.setOnClickListener(v -> devolverLibro());
        btnVolver.setOnClickListener(v -> finish());
    }

    private void loadBookDetails(int bookId) {
        bookRepository.getBookById(bookId, new BookRepository.ApiCallback<Book>() {
            @Override
            public void onSuccess(Book result) {
                book = result;
                txtAutor.setText("Autor: " + book.getAuthor());
                txtFecha.setText("Fecha: " + book.getPublishedDate());
                txtDescripcion.setText(book.getTitle());
                checkDisponible.setChecked(book.isAvailable());
                checkDisponible.setEnabled(false);

                // Configurar visibilidad de botones
                if (book.isAvailable()) {
                    btnPrestar.setVisibility(View.VISIBLE);
                    btnDevolver.setVisibility(View.GONE);
                } else {
                    btnPrestar.setVisibility(View.GONE);
                    btnDevolver.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(Detalle.this, "Error al cargar los detalles del libro", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void prestarLibro() {
        BookLendingForm lendingForm = new BookLendingForm(UserSingelton.getInstance().getUser().getId(), bookId);
        lendingRepository.lendBook(lendingForm, new BookRepository.ApiCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean success) {
                if (success) {
                    Toast.makeText(Detalle.this, "Libro prestado con éxito", Toast.LENGTH_SHORT).show();
                    book.setAvailable(false);
                    checkDisponible.setChecked(false);
                    btnPrestar.setVisibility(View.GONE);
                    btnDevolver.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(Detalle.this, "No se pudo prestar el libro", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(Detalle.this, "Error al prestar el libro", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void devolverLibro() {
        lendingRepository.returnBook(bookId, new BookRepository.ApiCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean success) {
                if (success) {
                    Toast.makeText(Detalle.this, "Libro devuelto con éxito", Toast.LENGTH_SHORT).show();
                    book.setAvailable(true);
                    checkDisponible.setChecked(true);
                    btnPrestar.setVisibility(View.VISIBLE);
                    btnDevolver.setVisibility(View.GONE);
                } else {
                    Toast.makeText(Detalle.this, "No se pudo devolver el libro", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(Detalle.this, "Error al devolver el libro", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

