package com.example.biblioteca;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.biblioteca.API.models.Book;
import com.example.biblioteca.API.models.UserSingelton;
import com.example.biblioteca.API.repository.BookRepository;
import com.example.biblioteca.API.repository.ImageRepository;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;

public class ListaBiblioteca extends AppCompatActivity {

    RecyclerView RvBiblioteca;
    Button BtnVolver, BtnUser2, BtnBuscar;
    EditText buscaTitle, buscaAuthor;
    private List<Book> bookList = new ArrayList<>();
    private List<Book> filteredList = new ArrayList<>();
    private BookRepository bookRepository;
    private MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lista_biblioteca);

        if (UserSingelton.getInstance().getUser() == null) {
            Log.e("LISTA_BIBLIOTECA", "No hay usuario en Singleton. Redirigiendo al Login.");
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        } else {
            Log.d("LISTA_BIBLIOTECA", "Usuario en Singleton: " + UserSingelton.getInstance().getUser().getEmail());
        }

        RvBiblioteca = findViewById(R.id.RvBiblioteca);
        BtnUser2 = findViewById(R.id.btnUsuario2);
        BtnVolver = findViewById(R.id.btnVolver);
        BtnBuscar = findViewById(R.id.btnBuscar);
        buscaTitle = findViewById(R.id.buscaTitle);
        buscaAuthor = findViewById(R.id.buscaAuthor);

        bookRepository = new BookRepository();

        RvBiblioteca.setLayoutManager(new LinearLayoutManager(this));

        // Inicialice el adaptader con filteredlist (hasta que se busquen libros es igual a bookList)
        adapter = new MyAdapter(filteredList);
        RvBiblioteca.setAdapter(adapter);

        loadBooks();

        BtnUser2.setOnClickListener(v -> startActivity(new Intent(v.getContext(), Perfil.class)));
        BtnVolver.setOnClickListener(v -> startActivity(new Intent(v.getContext(), InicioActivity.class)));
        BtnBuscar.setOnClickListener(v -> buscarLibros());
    }

    // Cargar libros desde la API
    private void loadBooks() {
        bookRepository.getBooks(new BookRepository.ApiCallback<List<Book>>() {
            @Override
            public void onSuccess(List<Book> result) {
                bookList = result;
                filteredList.clear();
                filteredList.addAll(bookList);
                adapter.notifyDataSetChanged(); // Refrescamos la lista completa en la RecyclerView
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(ListaBiblioteca.this, "Error al cargar los libros", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Filtrar libros por título y autor
    private void buscarLibros() {
        String titleFilter = buscaTitle.getText().toString().toLowerCase().trim();
        String authorFilter = buscaAuthor.getText().toString().toLowerCase().trim();

        filteredList.clear();

        for (Book book : bookList) {
            boolean matchesTitle = titleFilter.isEmpty() || book.getTitle().toLowerCase().contains(titleFilter);
            boolean matchesAuthor = authorFilter.isEmpty() || book.getAuthor().toLowerCase().contains(authorFilter);
            if (matchesTitle && matchesAuthor) {
                filteredList.add(book);
            }
        }

        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No se encontraron resultados", Toast.LENGTH_SHORT).show();
        }

        adapter.notifyDataSetChanged(); // notificar cambios a la lista
    }

    // Adaptador
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
                    if (result != null) {
                        holder.imageView.setImageBitmap(BitmapFactory.decodeStream(result.byteStream()));
                    } else {
                        holder.imageView.setImageResource(R.drawable.exception);
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    holder.imageView.setImageResource(R.drawable.cronica);
                }
            });

            // Checkbox marcado si el libro esta disponible
            holder.checkBoxDisponible.setChecked(book.isAvailable());
            holder.checkBoxDisponible.setEnabled(false);

            holder.btnDetalles.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), Detalle.class);
                intent.putExtra("bookId", book.getId());
                intent.putExtra("userId", UserSingelton.getInstance().getUser().getId());
                v.getContext().startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return books.size();
        }
    }
}
