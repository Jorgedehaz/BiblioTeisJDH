package com.example.biblioteca;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.biblioteca.API.models.Book;
import com.example.biblioteca.API.models.UserSingelton;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.List;

public class InicioActivity extends AppCompatActivity {

    RecyclerView RvInicio;
    Button BtnUser, Btnbiblioteca;
    private List<Book> bookList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verificamos si hay usuario en el Singleton
        if (UserSingelton.getInstance().getUser() == null) {
            Log.e("INICIO", "No hay usuario en Singleton. Redirigiendo al Login.");
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        } else {
            Log.d("INICIO", "Usuario en Singleton: " + UserSingelton.getInstance().getUser().getEmail());
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inicio);

        RvInicio = findViewById(R.id.RvInicio);
        BtnUser = findViewById(R.id.btnUser);
        Btnbiblioteca = findViewById(R.id.btnBiblioteca);

        // Libros hardcodeados, se podrian subir a la api
        bookList.add(new Book(1, "Alicia en el País de las Maravillas", "Lewis Carroll", "1865"));
        bookList.add(new Book(2, "Crónica de una Muerte Anunciada", "Gabriel García Márquez", "1981"));
        bookList.add(new Book(3, "El Señor de los Anillos", "J.R.R. Tolkien", "1954"));

        // Configurar RecyclerView
        RvInicio.setLayoutManager(new LinearLayoutManager(this));
        RvInicio.setAdapter(new MyAdapter(bookList));

        // Listeners de botones generales
        BtnUser.setOnClickListener(v -> startActivity(new Intent(v.getContext(), Perfil.class)));
        Btnbiblioteca.setOnClickListener(v -> startActivity(new Intent(v.getContext(), ListaBiblioteca.class)));


        //Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    // Inflater del menú en la Toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // listeners de las opciones de la Toolbar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int itemId = item.getItemId();

        if (itemId == R.id.action_inicio)
             startActivity(new Intent(this, InicioActivity.class));
        if (itemId == R.id.action_lista)
             startActivity(new Intent(this, ListaBiblioteca.class));
        if (itemId == R.id.action_perfil)
             startActivity(new Intent(this, Perfil.class));
        if (itemId == R.id.action_camera)
            escanearQR();

        return super.onOptionsItemSelected(item);
    }

    // Método para escan de QR , mejor en una clase a parte y llamarlo (?)
    private void escanearQR() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Escanea un código QR");
        integrator.setCameraId(0); // 0 = Cam trasera 1 = frontal
        integrator.setBarcodeImageEnabled(false); // No guardar imagen
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                // Si escanea correctamente, redirigir a ListaBiblioteca
                startActivity(new Intent(this, ListaBiblioteca.class));
            } else {
                Toast.makeText(this, "Escaneo cancelado", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }



    // Adaptador del RecyclerView
    class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        private final List<Book> books;
        private final int[] imageData = {R.drawable.alicia, R.drawable.cronica, R.drawable.esdla};

        public MyAdapter(List<Book> books) {
            this.books = books;
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView textView;
            ImageView imageView;
            Button detailBtn;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.txtlibroini);
                imageView = itemView.findViewById(R.id.imgLibroini);
                detailBtn = itemView.findViewById(R.id.btnlibroini);
            }
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_image, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            Book book = books.get(position);

            if (book != null) {
                holder.textView.setText(book.getTitle());
                holder.imageView.setImageResource(imageData[position]);

                // Configurar botón detalles
                holder.detailBtn.setOnClickListener(v -> {
                    Intent intent = new Intent(v.getContext(), Detalle.class);
                    intent.putExtra("title", book.getTitle() != null ? book.getTitle() : "Título no disponible");
                    intent.putExtra("author", book.getAuthor() != null ? book.getAuthor() : "Autor no disponible");
                    intent.putExtra("date", book.getPublishedDate() != null ? book.getPublishedDate() : "Fecha no disponible");
                    intent.putExtra("imageResId", imageData[position]);
                    intent.putExtra("isAvailable", true);
                    intent.putExtra("fromInicio", true); // usaremos esto para indicar que el libro es hardocodeado y filtrar en detalles
                    v.getContext().startActivity(intent);
                });
            }
        }


        @Override
        public int getItemCount() {
            return books.size();
        }
    }
}
