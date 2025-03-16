package com.example.biblioteca;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.biblioteca.API.models.Book;
import com.example.biblioteca.API.models.BookLending;
import com.example.biblioteca.API.models.User;
import com.example.biblioteca.API.repository.BookLendingRepository;
import com.example.biblioteca.API.repository.ImageRepository;
import com.example.biblioteca.API.repository.BookRepository;
import com.example.biblioteca.helper.Helper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;

public class Perfil extends AppCompatActivity {

    private TextView tVNombre, tVEmai, tVFecha;
    private ImageView imgPerfil;
    private ListView listaLibros;
    private Button btnVolver;
    private ImageRepository imageRepository;
    private BookLendingRepository lendingRepository;
    private Helper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        // Recuperar sesión del usuario
        SessionManager sessionManager = new SessionManager(this);
        User currentUser = sessionManager.getUser();

        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        Log.d("PERFIL", "Usuario en sesión: " + currentUser.getEmail());

        tVNombre = findViewById(R.id.nombrePerfil);
        tVEmai = findViewById(R.id.mailPerfil);
        tVFecha = findViewById(R.id.fechaPerfil);
        imgPerfil = findViewById(R.id.imagePerfil);
        listaLibros = findViewById(R.id.listaPerfil);
        btnVolver = findViewById(R.id.volverPerfil);

        imageRepository = new ImageRepository();
        lendingRepository = new BookLendingRepository();

        btnVolver.setOnClickListener(v -> startActivity(new Intent(v.getContext(), InicioActivity.class)));

        cargarDatosUsuario();
        obtenerPrestamosDesdeAPI(); // Llamada para cargar la lista de prestamos de cada user

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
        super.onActivityResult(requestCode, resultCode, data);
        helper.handleQRResult(requestCode, resultCode, data);
    }

    private void cargarDatosUsuario() {
        SessionManager sessionManager = new SessionManager(this);
        User usuario = sessionManager.getUser();
        if (usuario == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        if (usuario != null) {
            tVNombre.setText("Nombre: " + usuario.getName());
            tVEmai.setText("Email: " + usuario.getEmail());
            tVFecha.setText("Fecha Alta: " + usuario.getDateJoined());


            // Cargar imagen de perfil
            if (usuario.getProfilePicture() != null && !usuario.getProfilePicture().isEmpty()) {
                getImage(usuario.getProfilePicture());
            } else {
                imgPerfil.setImageResource(R.drawable.exception);
            }
        }
    }

    private void obtenerPrestamosDesdeAPI() {
        lendingRepository.getAllLendings(new BookRepository.ApiCallback<List<BookLending>>() {
            @Override
            public void onSuccess(List<BookLending> prestamos) {
                SessionManager sessionManager = new SessionManager(Perfil.this);
                User usuario = sessionManager.getUser();
                List<BookLending> prestamosUsuario = new ArrayList<>();

                for (BookLending prestamo : prestamos) {
                    if (prestamo.getUserId() == usuario.getId()) {
                        prestamosUsuario.add(prestamo);
                    }
                }

                if (prestamosUsuario.isEmpty()) {
                    runOnUiThread(() -> cargarListaLibros(prestamosUsuario));
                    return;
                }

                //ordenar la lista
                prestamosUsuario.sort((p1, p2) -> p2.getLendDate().compareTo(p1.getLendDate()));


                runOnUiThread(() -> cargarListaLibros(prestamosUsuario));
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("Perfil", "Error cargando prestamos", t);
            }
        });
    }

    //clase auxiliar para poder almacenar los datos y que funcione el acceso desde lista. Lo intente
    //con una lista paralela dentro del metodo por eso los accesos a detalles no se me correspondian con
    //la lista que se ve en la view. Las ids no se ordenaban correctamente y se mezclaban las ids reales con lo que veia el usuario
    class LibroInfo {
        Book book;
        BookLending lending;

        LibroInfo(Book book, BookLending lending) {
            this.book = book;
            this.lending = lending;
        }
    }


    private void cargarListaLibros(List<BookLending> bookLendings) {
        if (bookLendings == null || bookLendings.isEmpty()) return;

        List<LibroInfo> librosCompletos = new ArrayList<>();
        BookRepository bookRepository = new BookRepository();

        for (BookLending prestamo : bookLendings) {
            bookRepository.getBookById(prestamo.getBookId(), new BookRepository.ApiCallback<Book>() {
                @Override
                public void onSuccess(Book book) {
                    synchronized (librosCompletos) {
                        librosCompletos.add(new LibroInfo(book, prestamo));
                    }

                    // Solo cuando se hayan añadido TODOS los libros
                    if (librosCompletos.size() == bookLendings.size()) {
                        runOnUiThread(() -> {
                            // Ordenar solo una vez al final
                            librosCompletos.sort((a, b) -> b.lending.getLendDate().compareTo(a.lending.getLendDate()));

                            List<String> librosInfoText = new ArrayList<>();
                            for (LibroInfo item : librosCompletos) {
                                String lendDateFormatted = helper.formatDate(item.lending.getLendDate());
                                String returnDateFormatted = (item.lending.getReturnDate() != null) ? helper.formatDate(item.lending.getReturnDate()) : "No devuelto";

                                librosInfoText.add(item.book.getTitle() + " - Prestado: " + lendDateFormatted + " - Devolución: " + returnDateFormatted);
                            }

                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(Perfil.this, android.R.layout.simple_list_item_1, librosInfoText) {
                                @Override
                                public View getView(int position, View convertView, ViewGroup parent) {
                                    View view = super.getView(position, convertView, parent);
                                    TextView textView = (TextView) view;

                                    LibroInfo item = librosCompletos.get(position);
                                    String fechaEsperada = helper.calcularFechaDevolucion(item.lending.getLendDate());
                                    boolean vencido = helper.isFechaVenida(fechaEsperada) && item.lending.getReturnDate() == null;

                                    textView.setTextColor(vencido ? Color.RED : Color.BLACK);
                                    return view;
                                }
                            };

                            listaLibros.setAdapter(adapter);

                            // Click solo si el libro no se devolvió todavía
                            listaLibros.setOnItemClickListener((parent, view, position, id) -> {
                                LibroInfo item = librosCompletos.get(position);
                                if (item.lending.getReturnDate() == null) {
                                    Intent intent = new Intent(Perfil.this, Detalle.class);
                                    intent.putExtra("bookId", item.book.getId());
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(Perfil.this, "Este libro ya fue devuelto.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        });
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.e("Perfil", "Error obteniendo libro", t);
                }
            });
        }
    }







    private void getImage(String imageName) {
        imageRepository.getImage(imageName, new BookRepository.ApiCallback<ResponseBody>() {
            @Override
            public void onSuccess(ResponseBody responseBody) {
                if (responseBody != null) {
                    InputStream inputStream = responseBody.byteStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    runOnUiThread(() -> imgPerfil.setImageBitmap(bitmap));
                }else
                    runOnUiThread(() -> imgPerfil.setImageResource(R.drawable.exception));
            }

            @Override
            public void onFailure(Throwable t) {
                runOnUiThread(() -> imgPerfil.setImageResource(R.drawable.exception));
            }
        });
    }
}
