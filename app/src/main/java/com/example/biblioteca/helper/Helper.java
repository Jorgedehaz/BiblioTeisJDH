package com.example.biblioteca.helper;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import com.example.biblioteca.API.models.Book;
import com.example.biblioteca.API.repository.BookRepository;
import com.example.biblioteca.Detalle;
import com.example.biblioteca.InicioActivity;
import com.example.biblioteca.ListaBiblioteca;
import com.example.biblioteca.LoginActivity;
import com.example.biblioteca.Perfil;
import com.example.biblioteca.R;
import com.example.biblioteca.SessionManager;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Helper {
    private final AppCompatActivity activity;
    private final BookRepository bookRepository;


    public Helper(AppCompatActivity activity) {
        this.activity = activity;
        this.bookRepository = new BookRepository();
    }

    // Configura la Toolbar en la Activity
    public void setupToolbar() {
        Toolbar toolbar = activity.findViewById(R.id.toolbar);
        if (toolbar != null) {
            activity.setSupportActionBar(toolbar);
        } else {
            Log.e("TOOLBAR_HELPER", " Error en " + activity.getLocalClassName());
        }
    }

    // Manejo de eventos de la Toolbar
    public boolean handleMenuItemClick(@NonNull MenuItem item) {
        Context context = activity.getApplicationContext();
        int itemId = item.getItemId();

        if (itemId == R.id.action_inicio) {
            activity.startActivity(new Intent(context, InicioActivity.class));
        } else if (itemId == R.id.action_lista) {
            activity.startActivity(new Intent(context, ListaBiblioteca.class));
        } else if (itemId == R.id.action_perfil) {
            activity.startActivity(new Intent(context, Perfil.class));
        } else if (itemId == R.id.action_camera) {
            startQRScanner();
        } else if (itemId == R.id.action_logout) {
            SessionManager sessionManager = new SessionManager(context);
            sessionManager.logout();
            activity.startActivity(new Intent(context, LoginActivity.class));
            activity.finish();
        } else {
            return false;
        }

        return true;
    }

    // inflate del menú de la Toolbar
    public boolean onCreateOptionsMenu(Menu menu) {
        activity.getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    // Inicia el escaneo de QR
    public void startQRScanner() {
        IntentIntegrator integrator = new IntentIntegrator(activity);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Escanea un código QR");
        integrator.setCameraId(1);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }

    public void handleQRResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null) {
            String scannedISBN = result.getContents();
            Toast.makeText(activity, scannedISBN, Toast.LENGTH_SHORT).show();
            int scannedId = 1;
            buscarLibroPorISBN(scannedId);
        } else {
            Toast.makeText(activity, "Escaneo cancelado o sin resultado", Toast.LENGTH_SHORT).show();
        }
    }

    // Manejo del resultado del escaneo de QR
    private void buscarLibroPorISBN(int isbn) {

        bookRepository.getBookById(isbn, new BookRepository.ApiCallback<Book>() {
            @Override
            public void onSuccess(Book result) {
                if (isbn != 0) {
                    Intent intent = new Intent(activity, Detalle.class);
                    intent.putExtra("bookId", isbn);
                    activity.startActivity(intent);
                } else {
                    Toast.makeText(activity, "Libro no encontrado", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(activity, "Error al buscar el libro", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Método para formatear fechas
    public String formatDate(String date) {
        if (date == null || date.isEmpty()) return "Fecha no disponible";

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            return outputFormat.format(inputFormat.parse(date));
        } catch (Exception e) {
            return "Fecha no disponible";
        }
    }

    //Calcular si la fecha de devolución
    public String calcularFechaDevolucion(String lendDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat sdfOutput = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sdf.parse(lendDate));
            calendar.add(Calendar.DAY_OF_MONTH, 15); // Sumar 15 días

            return sdfOutput.format(calendar.getTime());
        } catch (Exception e) {
            return "No disponible";
        }
    }

    //Comprueba si se paso de la fecha de devolucion
    public boolean isFechaVenida(String returnDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            Calendar returnDateCalendar = Calendar.getInstance();
            returnDateCalendar.setTime(sdf.parse(returnDate));

            Calendar today = Calendar.getInstance();

            return today.after(returnDateCalendar);
        } catch (Exception e) {
            return false;
        }
    }





}
