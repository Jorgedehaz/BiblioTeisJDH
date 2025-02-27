package com.example.biblioteca;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.biblioteca.API.models.Book;

import java.util.ArrayList;
import java.util.List;

public class InicioActivity extends AppCompatActivity {

    RecyclerView RvInicio;
    Button BtnUser, Btnbiblioteca;
    private List<Book> bookList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inicio);

        RvInicio = findViewById(R.id.RvInicio);
        BtnUser = findViewById(R.id.btnUser);
        Btnbiblioteca = findViewById(R.id.btnBiblioteca);

        // Libros hardcodeados, se podrian subir a la ap
        bookList.add(new Book(1, "Alicia en el País de las Maravillas", "Lewis Carroll", "1865"));
        bookList.add(new Book(2, "Crónica de una Muerte Anunciada", "Gabriel García Márquez", "1981"));
        bookList.add(new Book(3, "El Señor de los Anillos", "J.R.R. Tolkien", "1954"));

        // Configurar RecyclerView
        RvInicio.setLayoutManager(new LinearLayoutManager(this));
        RvInicio.setAdapter(new MyAdapter(bookList));

        // Listeners de botones generales
        BtnUser.setOnClickListener(v -> startActivity(new Intent(v.getContext(), Perfil.class)));
        Btnbiblioteca.setOnClickListener(v -> startActivity(new Intent(v.getContext(), ListaBiblioteca.class)));
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
