package com.example.biblioteca.API.retrofit;

import com.example.biblioteca.API.models.Book;
import com.example.biblioteca.API.models.BookLending;
import com.example.biblioteca.API.models.BookLendingForm;
import com.example.biblioteca.API.models.User;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    // Books Endpoints
    @GET("books")
    Call<List<Book>> getBooks();

    @GET("books/{id}")
    Call<Book> getBook(@Path("id") int id);

    @POST("books")
    Call<Book> createBook(@Body Book book);

    @PUT("books/{id}")
    Call<Void> updateBook(@Path("id") int id, @Body Book book);

    @DELETE("books/{id}")
    Call<Void> deleteBook(@Path("id") int id);

    // Users Endpoints
    @GET("users")
    Call<List<User>> getUsers();

    @GET("users/{id}")
    Call<User> getUser(@Path("id") int id);

    @POST("users")
    Call<User> createUser(@Body User user);

    @PUT("users/{id}")
    Call<Void> updateUser(@Path("id") int id, @Body User user);

    @DELETE("users/{id}")
    Call<Void> deleteUser(@Path("id") int id);

    // Book Lending Endpoints
    @GET("booklending")
    Call<List<BookLending>> getLendings();

    @GET("booklending/{id}")
    Call<BookLending> getLending(@Path("id") int id);

    //@POST("booklending")
    //Call<BookLending> lendBook(@Body BookLendingForm lending);

    //He tenido que hacer este cambio para poder prestar libros.
    //La API podría no estar esperando estos valores en el cuerpo, sino en los parámetros de la URL.
    //Esto me dijo GPT al hacer la peticion manualmente en swagger y enseñale el proceso y resultado.
    @POST("booklending")
    Call<BookLending> lendBook(@Query("userId") int userId, @Query("bookId") int bookId);


    @PUT("booklending/{id}/return")
    Call<Void> returnBook(@Path("id") int id);

    @GET("image/{filename}")
    Call<ResponseBody> getImage(@Path("filename") String fileName);

}
