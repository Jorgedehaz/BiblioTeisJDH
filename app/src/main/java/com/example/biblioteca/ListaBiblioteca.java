package com.example.biblioteca;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import com.example.biblioteca.API.models.Book;
import com.example.biblioteca.API.models.User;
import com.example.biblioteca.API.models.UserSingelton;
import com.example.biblioteca.API.repository.BookRepository;
import com.example.biblioteca.API.repository.ImageRepository;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

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
    private BookViewModel bookViewModel;
    private MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lista_biblioteca);

        SessionManager sessionManager = new SessionManager(this);
        User currentUser = sessionManager.getUser();

        // Si no hay usuario guardado, redirigir a Login
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        Log.d("INICIO", "Usuario en sesión: " + currentUser.getEmail());

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

        // Usamos ViewModel
        bookViewModel = new ViewModelProvider(this).get(BookViewModel.class);
        bookViewModel.getBooksLiveData().observe(this, books -> {
            if (books != null) {
                filteredList.clear();
                filteredList.addAll(books);
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Error al cargar los libros", Toast.LENGTH_SHORT).show();
            }
        });

        BtnUser2.setOnClickListener(v -> startActivity(new Intent(v.getContext(), Perfil.class)));
        BtnVolver.setOnClickListener(v -> startActivity(new Intent(v.getContext(), InicioActivity.class)));
        BtnBuscar.setOnClickListener(v -> buscarLibros());

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
        if (itemId == R.id.action_logout) {
            SessionManager sessionManager = new SessionManager(this);
            sessionManager.logout(); // Borra los datos del usuario
            startActivity(new Intent(this, LoginActivity.class));
            finish(); // Cierra la actividad actual
        }

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
                // Obtener el usuario desde SharedPreferences en lugar del Singleton
                SessionManager sessionManager = new SessionManager(v.getContext());
                User currentUser = sessionManager.getUser();

                if (currentUser != null) {
                    Intent intent = new Intent(v.getContext(), Detalle.class);
                    intent.putExtra("bookId", book.getId());
                    intent.putExtra("userId", currentUser.getId());
                    v.getContext().startActivity(intent);
                } else {
                    Log.e("LISTA_BIBLIOTECA", "No hay usuario en sesión. Redirigiendo al login.");
                    v.getContext().startActivity(new Intent(v.getContext(), LoginActivity.class));
                }
            });
        }

        @Override
        public int getItemCount() {
            return books.size();
        }
    }
}
