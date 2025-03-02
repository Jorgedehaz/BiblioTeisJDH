package com.example.biblioteca.API.repository;

import android.util.Log;

import com.example.biblioteca.API.models.BookLending;
import com.example.biblioteca.API.models.BookLendingForm;
import com.example.biblioteca.API.retrofit.ApiClient;
import com.example.biblioteca.API.retrofit.ApiService;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookLendingRepository {
    private ApiService apiService;

    public BookLendingRepository() {
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    public void getAllLendings(final BookRepository.ApiCallback<List<BookLending>> callback) {
        apiService.getLendings().enqueue(new Callback<List<BookLending>>() {
            @Override
            public void onResponse(Call<List<BookLending>> call, Response<List<BookLending>> response) {
                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<List<BookLending>> call, Throwable t) {
                Log.e("BookLendingRepository", "Error fetching lendings", t);
                callback.onFailure(t);
            }
        });
    }

    public void lendBook(int userId, int bookId, final BookRepository.ApiCallback<Boolean> callback) {

        //Debuggin para saber que recibo en el repository , los datos parecen pasarse bien desde Detalles.java
        Log.d("LENDING", "Datos enviados a repository - userId: " + userId + ", bookId: " + bookId);

        apiService.lendBook(userId, bookId).enqueue(new Callback<BookLending>() {
            @Override
            public void onResponse(Call<BookLending> call, Response<BookLending> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("LENDING", "Libro prestado con éxito - Book ID: " + response.body().getBookId());
                    callback.onSuccess(true);
                } else {
                    String errorMessage = "";
                    try {
                        if (response.errorBody() != null) {
                            errorMessage = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        errorMessage = "Error al leer el mensaje de error.";
                    }

                    //Debuggin para ver el error que devuelve
                    Log.e("LENDING", "Error al prestar libro. Código HTTP: " + response.code() + " - Mensaje: " + response.message());
                    Log.e("LENDING", "Cuerpo de la respuesta: " + errorMessage); // error real

                    callback.onSuccess(false);
                }
            }



            @Override
            public void onFailure(Call<BookLending> call, Throwable t) {
                Log.e("BookLendingRepository", "Error lending book", t);
                callback.onFailure(t);
            }
        });
    }

    public void returnBook(int id, final BookRepository.ApiCallback<Boolean> callback) {
        apiService.returnBook(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                callback.onSuccess(response.isSuccessful());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("BookLendingRepository", "Error returning book", t);
                callback.onFailure(t);
            }
        });
    }
}

