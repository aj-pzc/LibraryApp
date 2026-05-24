package com.ebac.project.objects;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String name;
    private String email;
    private String password;
    private List<Book> borrowedBooks;

    public List<Book> getBorrowedBooks() { return borrowedBooks; }
    public void setBorrowedBooks(List<Book> borrowedBooks) { this.borrowedBooks = borrowedBooks; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email;}
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public User(String name, String emal, String password) {
        this.name = name;
        this.email = emal;
        this.password = password;
        this.borrowedBooks = new ArrayList<>();
    }

    public void borrowBook(Book book) {
        this.borrowedBooks.add(book);
    }

    public void returnBook(Book book) {
        this.borrowedBooks.remove(book);
    }
}
