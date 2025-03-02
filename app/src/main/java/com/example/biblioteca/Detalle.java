package com.example.biblioteca;

import android.content.Intent;
import android.graphics.BitmapFactory;
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
import com.example.biblioteca.API.models.User;
import com.example.biblioteca.API.models.UserSingelton;
import com.example.biblioteca.API.repository.BookLendingRepository;
import com.example.biblioteca.API.repository.BookRepository;
import com.example.biblioteca.API.repository.ImageRepository;
import com.example.biblioteca.API.repository.UserRepository;
import com.example.biblioteca.API.repository.BookRepository.ApiCallback;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import okhttp3.ResponseBody;

public class Detalle extends AppCompatActivity {
    private TextView txtAutor, txtFecha, txtDescripcion;
    private ImageView imgDetalle;
    private CheckBox checkDisponible;
    private Button btnPrestar, btnDevolver, btnVolver;
    private BookRepository bookRepository;
    private BookLendingRepository lendingRepository;
    private UserRepository userRepository;
    private int bookId;
    private Book book;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle);

        if (UserSingelton.getInstance().getUser() == null) {
            int userId = getIntent().getIntExtra("userId", -1);

            if (userId != -1) {
                userRepository = new UserRepository();
                userRepository.getUserById(userId, new ApiCallback<User>() {
                    @Override
                    public void onSuccess(User user) {
                        UserSingelton.getInstance().setUser(user);
                        Log.d("DETALLE", "✅ Usuario restaurado en Singleton desde la API: " + user.getEmail());

                    }
                    @Override
                    public void onFailure(Throwable t) {
                        Log.e("DETALLE", "Error al restaurar usuario desde API. Redirigiendo al Login.");
                        Intent intent = new Intent(Detalle.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
            } else {
                Log.e("DETALLE", "No hay userId en Intent. Redirigiendo al Login.");
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        } else {
            Log.d("DETALLE", "✅ Usuario ya presente en Singleton: " + UserSingelton.getInstance().getUser().getEmail());

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

        Log.d("DETALLE", "ID del libro recibido en Detalle: " + bookId);

        if (bookId != -1) {
            //El libro viene de listabiblioteca, cargar desde la API
            loadBookDetails(bookId);
        } else {
            //El libro viene de inicio, cargar datos desde intent
            String title = getIntent().getStringExtra("title");
            String author = getIntent().getStringExtra("author");
            String date = getIntent().getStringExtra("date");
            int imageResId = getIntent().getIntExtra("imageResId", R.drawable.exception);
            boolean isAvailable = getIntent().getBooleanExtra("isAvailable", true);

            // Cargar datos
            txtDescripcion.setText(title);
            txtAutor.setText("Autor: " + author);
            txtFecha.setText("Fecha: " + date);
            imgDetalle.setImageResource(imageResId);

            // Configurar disponibilidad
            checkDisponible.setChecked(isAvailable);
            checkDisponible.setEnabled(false);

            // oculto las opciones si el libro es hardcodeado
            btnPrestar.setVisibility(View.GONE);
            btnDevolver.setVisibility(View.GONE);
        }

        btnPrestar.setOnClickListener(v -> prestarLibro());
        btnDevolver.setOnClickListener(v ->devolverLibro());
        if (bookId != -1) {
            btnVolver.setOnClickListener(v -> startActivity(new Intent(v.getContext(), ListaBiblioteca.class)));
        }else
            btnVolver.setOnClickListener(v -> startActivity(new Intent(v.getContext(), InicioActivity.class)));
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

                Log.d("DETALLE", "Libro ID: " + book.getId() + " - Disponible desde API: " + book.isAvailable());

                //cargamos la img del libro de detalle
                cargarImgLibro();

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

    private void cargarImgLibro() {
        ImageRepository ir = new ImageRepository();
        ir.getImage(book.getBookPicture(), new BookRepository.ApiCallback<ResponseBody>() {
            @Override
            public void onSuccess(ResponseBody result) {
                if (result != null) {
                    imgDetalle.setImageBitmap(BitmapFactory.decodeStream(result.byteStream()));
                } else {
                    imgDetalle.setImageResource(R.drawable.exception); // Imagen por defecto si falla
                }
            }

            @Override
            public void onFailure(Throwable t) {
                imgDetalle.setImageResource(R.drawable.exception); // Imagen de error si falla la carga
            }
        });
    }

    private void prestarLibro() {

        int userId = UserSingelton.getInstance().getUser().getId();
        int bookId = this.book.getId();

        Log.d("LENDING", " userID " + userId + " bookId " + bookId);
        Log.d("LENDING", "Libro disponible : " + book.isAvailable());

        if (!book.isAvailable()) {
            Log.e("LENDING", "El libro NO está disponible según la app.");
            Toast.makeText(Detalle.this, "El libro no está disponible.", Toast.LENGTH_SHORT).show();
            return;
        }


        BookLendingForm lendingForm = new BookLendingForm(userId, bookId);

        lendingRepository.lendBook(userId, bookId, new BookRepository.ApiCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean success) {
                if (success) {
                    Log.d("LENDING", "Préstamo realizado con éxito");
                    Toast.makeText(Detalle.this, "Libro prestado con éxito", Toast.LENGTH_SHORT).show();
                    book.setAvailable(false);
                    checkDisponible.setChecked(false);
                    btnPrestar.setVisibility(View.GONE);
                    btnDevolver.setVisibility(View.VISIBLE);
                } else {
                    Log.e("LENDING", "No se pudo prestar el libro");
                    Toast.makeText(Detalle.this, "No se pudo prestar el libro", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("LENDING", "Error al prestar libro", t);
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

