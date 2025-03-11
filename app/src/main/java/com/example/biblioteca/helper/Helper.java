package com.example.biblioteca.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import com.example.biblioteca.InicioActivity;
import com.example.biblioteca.ListaBiblioteca;
import com.example.biblioteca.LoginActivity;
import com.example.biblioteca.Perfil;
import com.example.biblioteca.R;
import com.example.biblioteca.SessionManager;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class Helper {
    private final AppCompatActivity activity;

    public Helper(AppCompatActivity activity) {
        this.activity = activity;
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

    public void startQRScanner() {
        IntentIntegrator integrator = new IntentIntegrator(activity);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Escanea un código QR");
        integrator.setCameraId(0);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }

    // Manejo del resultado del escaneo de QR
    public void handleQRResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                // Redirigir a ListaBiblioteca tras escanear correctamente
                activity.startActivity(new Intent(activity, ListaBiblioteca.class));
            } else {
                Toast.makeText(activity, "Escaneo cancelado", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
