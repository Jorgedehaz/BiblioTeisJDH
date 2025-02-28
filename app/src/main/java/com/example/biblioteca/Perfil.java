package com.example.biblioteca;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.biblioteca.API.models.Book;
import com.example.biblioteca.API.models.BookLending;
import com.example.biblioteca.API.models.User;
import com.example.biblioteca.API.models.UserSingelton;
import com.example.biblioteca.API.repository.BookLendingRepository;
import com.example.biblioteca.API.repository.ImageRepository;
import com.example.biblioteca.API.repository.BookRepository;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verificamos si hay usuario en el Singleton
        if (UserSingelton.getInstance().getUser() == null) {
            Log.e("PERFIL", "Usuario en Singleton es NULL. Redirigiendo al Login.");
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        } else {
            Log.d("PERFIL", "Usuario en Singleton: " + UserSingelton.getInstance().getUser().getEmail());
        }

        setContentView(R.layout.activity_perfil);

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
    }

    private void cargarDatosUsuario() {
        User usuario = UserSingelton.getInstance().getUser();
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
                User usuario = UserSingelton.getInstance().getUser();
                List<BookLending> prestamosUsuario = new ArrayList<>();

                for (BookLending prestamo : prestamos) {
                    if (prestamo.getUserId() == usuario.getId()) { // la api devuelve el id , por lo que tuve que compararlas con el user de la sesion
                        prestamosUsuario.add(prestamo);
                    }
                }
                runOnUiThread(() -> cargarListaLibros(prestamosUsuario));
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("Perfil", "Error cargando prestamos", t);
            }
        });
    }


    private void cargarListaLibros(List<BookLending> bookLendings) {
        if (bookLendings != null && !bookLendings.isEmpty()) {
            List<String> librosInfo = new ArrayList<>();
            BookRepository bookRepository = new BookRepository();

            for (BookLending prestamo : bookLendings) {
                bookRepository.getBookById(prestamo.getBookId(), new BookRepository.ApiCallback<Book>() {
                    @Override
                    public void onSuccess(Book book) {
                        String info = book.getTitle() + " - Fehca préstamo: " + prestamo.getLendDate();
                        librosInfo.add(info);
                        runOnUiThread(() -> {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(Perfil.this, android.R.layout.simple_list_item_1, librosInfo);
                            listaLibros.setAdapter(adapter);
                        });
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Log.e("Perfil", "Error obteniendo libro", t);
                    }
                });
            }
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
