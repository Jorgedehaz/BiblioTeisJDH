package com.example.biblioteca;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.biblioteca.API.models.Book;
import com.example.biblioteca.API.repository.BookRepository;

import java.util.List;

public class BookViewModel extends ViewModel {
    private final MutableLiveData<List<Book>> booksLiveData = new MutableLiveData<>();
    private final BookRepository bookRepository;

    public BookViewModel() {
        bookRepository = new BookRepository();
        loadBooks();
    }

    public LiveData<List<Book>> getBooksLiveData() {
        return booksLiveData;
    }

    public void loadBooks() {
        bookRepository.getBooks(new BookRepository.ApiCallback<List<Book>>() {
            @Override
            public void onSuccess(List<Book> result) {
                booksLiveData.postValue(result);
            }

            @Override
            public void onFailure(Throwable t) {
                booksLiveData.postValue(null);
            }
        });
    }
}
