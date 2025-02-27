package com.example.biblioteca.API.repository;

import android.util.Log;

import com.example.biblioteca.API.models.User;
import com.example.biblioteca.API.retrofit.ApiClient;
import com.example.biblioteca.API.retrofit.ApiService;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImageRepository {

    private ApiService apiService;

    public ImageRepository() {
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    public void getImage(String imageName, final BookRepository.ApiCallback<ResponseBody> callback){
        apiService.getImage(imageName).enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callback.onFailure(t);
            }
        });
    }
}
