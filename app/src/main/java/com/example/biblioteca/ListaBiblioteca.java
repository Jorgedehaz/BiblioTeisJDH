package com.example.biblioteca;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.biblioteca.API.models.Book;
import com.example.biblioteca.API.repository.BookRepository;
import com.example.biblioteca.API.repository.ImageRepository;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;

public class ListaBiblioteca extends AppCompatActivity {

    RecyclerView RvBiblioteca;
    Button BtnVolver, BtnUser2;
    private List<Book> bookList = new ArrayList<>();
    private BookRepository bookRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lista_biblioteca);

        RvBiblioteca = findViewById(R.id.RvBiblioteca);
        BtnUser2 = findViewById(R.id.btnUsuario2);
        BtnVolver = findViewById(R.id.btnVolver);
        bookRepository = new BookRepository();

        RvBiblioteca.setLayoutManager(new LinearLayoutManager(this));

        loadBooks();

        BtnUser2.setOnClickListener(v -> startActivity(new Intent(v.getContext(), Perfil.class)));
        BtnVolver.setOnClickListener(v -> startActivity(new Intent(v.getContext(), InicioActivity.class)));
    }

    //Cargar libros desde la api
    private void loadBooks() {
        bookRepository.getBooks(new BookRepository.ApiCallback<List<Book>>() {
            @Override
            public void onSuccess(List<Book> result) {
                bookList = result;
                RvBiblioteca.setAdapter(new MyAdapter(bookList));
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(ListaBiblioteca.this, "Error al cargar los libros", Toast.LENGTH_SHORT).show();
            }
        });
    }

    class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        private final List<Book> books;

        public MyAdapter(List<Book> books) {
            this.books = books;
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView textView;
            ImageView imageView;
            Button btnDetalles;
            CheckBox checkBoxDisponible;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.txtlibroini);
                imageView = itemView.findViewById(R.id.imgLibroini);
                btnDetalles = itemView.findViewById(R.id.btnlibroini);
                checkBoxDisponible = itemView.findViewById(R.id.checkDisponible);
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
            holder.textView.setText(book.getTitle());

            ImageRepository ir = new ImageRepository();
            ir.getImage(book.getBookPicture(), new BookRepository.ApiCallback<ResponseBody>() {
                @Override
                public void onSuccess(ResponseBody result) {
                    if (result != null)
                        holder.imageView.setImageBitmap(BitmapFactory.decodeStream(result.byteStream()));
                }

                @Override
                public void onFailure(Throwable t) {
                    holder.imageView.setImageResource(R.drawable.cronica);
                }
            });

            //ponemos checkbox marcada si esta disponible
            holder.checkBoxDisponible.setChecked(book.isAvailable());

            holder.btnDetalles.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), Detalle.class);
                intent.putExtra("bookId", book.getId());
                v.getContext().startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return books.size();
        }
    }
}
