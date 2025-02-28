package com.example.biblioteca.API.models;

public class BookLendingForm {
    private int userId;
    private int bookId;

    public BookLendingForm(int userId, int bookId) {
        this.userId = userId;
        this.bookId = bookId;
    }


    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }
}
