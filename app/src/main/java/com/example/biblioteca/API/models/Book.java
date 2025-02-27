package com.example.biblioteca.API.models;

import java.util.List;

public class Book {
    private int id;
    private String title;
    private String author;
    private String isbn;
    private String publishedDate;
    private String bookPicture;
    private boolean isAvailable;

    private List<BookLending> bookLendings;

    public Book(int id, String title, String author, String publishedDate) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.publishedDate = publishedDate;
    }

    // Getters & Setters


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(String publishedDate) {
        this.publishedDate = publishedDate;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public List<BookLending> getBookLendings() {
        return bookLendings;
    }

    public void setBookLendings(List<BookLending> bookLendings) {
        this.bookLendings = bookLendings;
    }

    public String getBookPicture() {
        return bookPicture;
    }

    public void setBookPicture(String bookPicture) {
        this.bookPicture = bookPicture;
    }
}
