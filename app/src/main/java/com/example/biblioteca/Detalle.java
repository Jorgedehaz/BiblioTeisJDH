package com.example.biblioteca;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.biblioteca.API.models.Book;
import com.example.biblioteca.API.repository.BookRepository;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Detalle extends AppCompatActivity {

    TextView txtTitulo, txtAutor, txtFecha, txtDevolucion;
    ImageView imgDetalle;
    CheckBox checkDisponible;
    Button btnVolver;
    private BookRepository bookRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalle);

        txtTitulo = findViewById(R.id.txtdescripcion);
        txtAutor = findViewById(R.id.txtAutor);
        txtFecha = findViewById(R.id.txtFecha);
        txtDevolucion = findViewById(R.id.txtDevolucion);
        imgDetalle = findViewById(R.id.imgdetalle);
        checkDisponible = findViewById(R.id.checkDisponible);
        btnVolver = findViewById(R.id.btnVolver);

        bookRepository = new BookRepository();

        Intent intent = getIntent();
        if (intent != null) {
            int bookId = intent.getIntExtra("bookId", -1);
            if (bookId != -1) {
                loadBookDetails(bookId);
            } else {
                Toast.makeText(this, "Error al obtener el libro", Toast.LENGTH_SHORT).show();
            }
        }

        btnVolver.setOnClickListener(v -> finish());
    }

    private void loadBookDetails(int bookId) {
        bookRepository.getBookById(bookId, new BookRepository.ApiCallback<Book>() {
            @Override
            public void onSuccess(Book book) {
                txtTitulo.setText(book.getTitle());
                txtAutor.setText("Autor: " + book.getAuthor());
                txtFecha.setText("Publicado: " + book.getPublishedDate());
                checkDisponible.setChecked(book.isAvailable());

                if (!book.isAvailable()) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.DAY_OF_YEAR, 7);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    txtDevolucion.setText("Fecha de devolución: " + sdf.format(calendar.getTime()));
                } else {
                    txtDevolucion.setText("");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(Detalle.this, "Error al cargar el libro", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
