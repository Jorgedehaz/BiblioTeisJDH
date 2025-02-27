package com.example.biblioteca.API.repository;

import android.util.Log;

import com.example.biblioteca.API.models.User;
import com.example.biblioteca.API.retrofit.ApiClient;
import com.example.biblioteca.API.retrofit.ApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {
    private ApiService apiService;

    public UserRepository() {
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    public void getUsers(final BookRepository.ApiCallback<List<User>> callback) {
        apiService.getUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Log.e("UserRepository", "Error fetching users", t);
                callback.onFailure(t);
            }
        });
    }

    public void getUserById(int id, final BookRepository.ApiCallback<User> callback) {
        apiService.getUser(id).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("UserRepository", "Error fetching user", t);
                callback.onFailure(t);
            }
        });
    }

    public void createUser(User user, final BookRepository.ApiCallback<Boolean> callback) {
        apiService.createUser(user).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                callback.onSuccess(response.isSuccessful());
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("UserRepository", "Error creating user", t);
                callback.onFailure(t);
            }
        });
    }

    public void updateUser(int id, User user, final BookRepository.ApiCallback<Boolean> callback) {
        apiService.updateUser(id, user).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                callback.onSuccess(response.isSuccessful());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("UserRepository", "Error updating user", t);
                callback.onFailure(t);
            }
        });
    }

    public void deleteUser(int id, final BookRepository.ApiCallback<Boolean> callback) {
        apiService.deleteUser(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                callback.onSuccess(response.isSuccessful());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("UserRepository", "Error deleting user", t);
                callback.onFailure(t);
            }
        });
    }
}
