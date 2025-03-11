package com.example.biblioteca;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.biblioteca.API.models.Book;
import com.example.biblioteca.API.models.BookLending;
import com.example.biblioteca.API.models.BookLendingForm;
import com.example.biblioteca.API.models.User;
import com.example.biblioteca.API.repository.BookLendingRepository;
import com.example.biblioteca.API.repository.BookRepository;
import com.example.biblioteca.API.repository.ImageRepository;
import com.example.biblioteca.SessionManager;
import com.example.biblioteca.helper.Helper;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;

public class Detalle extends AppCompatActivity {
    private TextView txtAutor, txtFecha, txtDescripcion, txtFechaDev;
    private ImageView imgDetalle;
    private CheckBox checkDisponible;
    private Button btnPrestar, btnDevolver, btnVolver;
    private BookRepository bookRepository;
    private BookLendingRepository lendingRepository;
    private int bookId;
    private Book book;
    private User currentUser;
    private Helper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle);

        // Recuperar usuario de sesión con SharedPreferences
        SessionManager sessionManager = new SessionManager(this);
        currentUser = sessionManager.getUser();

        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        Log.d("DETALLE", "Usuario en sesión: " + currentUser.getEmail());

        txtAutor = findViewById(R.id.txtAutor);
        txtFecha = findViewById(R.id.txtFecha);
        txtFechaDev = findViewById(R.id.txtFechaDev);
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
            loadBookDetails(bookId);
        } else {
            cargarDatosDesdeIntent();
        }

        btnPrestar.setOnClickListener(v -> prestarLibro());
        btnDevolver.setOnClickListener(v -> devolverLibro());

        btnVolver.setOnClickListener(v -> {
            if (bookId != -1) {
                startActivity(new Intent(v.getContext(), ListaBiblioteca.class));
            } else {
                startActivity(new Intent(v.getContext(), InicioActivity.class));
            }
        });

        //Toolbar
        helper = new Helper(this);
        helper.setupToolbar();
    }

    // Inflater del menú en la Toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return helper.onCreateOptionsMenu(menu);
    }

    // listeners de las opciones de la Toolbar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return helper.handleMenuItemClick(item) || super.onOptionsItemSelected(item);
    }

    //resultado del scan del qr
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        helper.handleQRResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
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

                cargarImgLibro();
                cargarFechaDevolucion();
                configurarBotones();
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(Detalle.this, "Error al cargar los detalles del libro", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarFechaDevolucion() {
        lendingRepository.getAllLendings(new BookRepository.ApiCallback<List<BookLending>>() {
            @Override
            public void onSuccess(List<BookLending> lendings) {
                for (BookLending lending : lendings) {
                    if (lending.getBookId() == book.getId()) {
                        String returnDate = calcularFechaDevolucion(lending.getLendDate());
                        txtFechaDev.setText("Fecha devolución: " + returnDate);
                        break;
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                txtFechaDev.setText("Fecha devolución: No disponible");
            }
        });
    }

    private String calcularFechaDevolucion(String lendDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sdf.parse(lendDate));
            calendar.add(Calendar.DAY_OF_MONTH, 15);
            return sdf.format(calendar.getTime());
        } catch (Exception e) {
            return "No disponible";
        }
    }

    private void cargarImgLibro() {
        ImageRepository ir = new ImageRepository();
        ir.getImage(book.getBookPicture(), new BookRepository.ApiCallback<ResponseBody>() {
            @Override
            public void onSuccess(ResponseBody result) {
                imgDetalle.setImageBitmap(BitmapFactory.decodeStream(result.byteStream()));
            }

            @Override
            public void onFailure(Throwable t) {
                imgDetalle.setImageResource(R.drawable.exception);
            }
        });
    }

    private void configurarBotones() {
        if (book.isAvailable()) {
            btnPrestar.setVisibility(View.VISIBLE);
            btnDevolver.setVisibility(View.GONE);
        } else {
            btnPrestar.setVisibility(View.GONE);
            btnDevolver.setVisibility(View.VISIBLE);
        }
    }

    private void cargarDatosDesdeIntent() {
        txtDescripcion.setText(getIntent().getStringExtra("title"));
        txtAutor.setText("Autor: " + getIntent().getStringExtra("author"));
        txtFecha.setText("Fecha: " + getIntent().getStringExtra("date"));
        imgDetalle.setImageResource(getIntent().getIntExtra("imageResId", R.drawable.exception));
        checkDisponible.setChecked(getIntent().getBooleanExtra("isAvailable", true));
        checkDisponible.setEnabled(false);
        btnPrestar.setVisibility(View.GONE);
        btnDevolver.setVisibility(View.GONE);
    }

    private void prestarLibro() {
        SessionManager sessionManager = new SessionManager(this);
        User currentUser = sessionManager.getUser();

        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        int userId = currentUser.getId();
        int bookId = this.book.getId();

        Log.d("LENDING", " userID " + userId + " bookId " + bookId);
        Log.d("LENDING", "Libro disponible : " + book.isAvailable());

        if (!book.isAvailable()) {
            Log.e("LENDING", "El libro NO está disponible según la app.");
            Toast.makeText(Detalle.this, "El libro no está disponible.", Toast.LENGTH_SHORT).show();
            return;
        }

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
        SessionManager sessionManager = new SessionManager(this);
        User currentUser = sessionManager.getUser();

        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        int userId = currentUser.getId();
        int bookId = this.book.getId();

        Log.d("RETURN", " userID " + userId + " bookId " + bookId);

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
