package com.example.biblioteca.API.repository;

import android.util.Log;

import com.example.biblioteca.API.models.Book;
import com.example.biblioteca.API.retrofit.ApiClient;
import com.example.biblioteca.API.retrofit.ApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookRepository {
    private ApiService apiService;

    public BookRepository() {
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    public void getBooks(final ApiCallback<List<Book>> callback) {
        apiService.getBooks().enqueue(new Callback<List<Book>>() {
            @Override
            public void onResponse(Call<List<Book>> call, Response<List<Book>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure(new Exception("Failed to fetch books"));
                }
            }

            @Override
            public void onFailure(Call<List<Book>> call, Throwable t) {
                Log.e("BookRepository", "Error fetching books", t);
                callback.onFailure(t);
            }
        });
    }

    public void getBookById(int id, final ApiCallback<Book> callback) {
        apiService.getBook(id).enqueue(new Callback<Book>() {
            @Override
            public void onResponse(Call<Book> call, Response<Book> response) {
                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<Book> call, Throwable t) {
                Log.e("BookRepository", "Error fetching book", t);
                callback.onFailure(t);
            }
        });
    }

    public void createBook(Book book, final ApiCallback<Boolean> callback) {
        apiService.createBook(book).enqueue(new Callback<Book>() {
            @Override
            public void onResponse(Call<Book> call, Response<Book> response) {
                callback.onSuccess(response.isSuccessful());
            }

            @Override
            public void onFailure(Call<Book> call, Throwable t) {
                Log.e("BookRepository", "Error creating book", t);
                callback.onFailure(t);
            }
        });
    }

    public void updateBook(int id, Book book, final ApiCallback<Boolean> callback) {
        apiService.updateBook(id, book).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                callback.onSuccess(response.isSuccessful());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("BookRepository", "Error updating book", t);
                callback.onFailure(t);
            }
        });
    }

    public void deleteBook(int id, final ApiCallback<Boolean> callback) {
        apiService.deleteBook(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                callback.onSuccess(response.isSuccessful());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("BookRepository", "Error deleting book", t);
                callback.onFailure(t);
            }
        });
    }

    // Callback interface for handling success or failure
    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onFailure(Throwable t);
    }
}
