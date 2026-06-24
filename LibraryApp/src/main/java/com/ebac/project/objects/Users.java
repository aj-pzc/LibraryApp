package com.ebac.project.objects;

import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class Users {

    private String name;
    private String lastName;
    private String email;
    private String username;
    private String password;
    private String phone;
    private List<Book> borrowedBooks;


    public Users(String name, String lastName, String email, String username, String password) {
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.borrowedBooks = new ArrayList<>();
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<Book> getBorrowedBooks() { return borrowedBooks; }
    public void setBorrowedBooks(List<Book> borrowedBooks) { this.borrowedBooks = borrowedBooks; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email;}
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public void borrowBook(Book book) {
        this.borrowedBooks.add(book);
    }

    public void returnBook(Book book) {
        this.borrowedBooks.removeIf(b-> b.getISBN().equals(book.getISBN()));
    }

    public Document userToDoc(){
        List<Document> borrowedBooks = this.borrowedBooks
                .stream()
                .map(Book::borrowedBooksDoc)
                .toList();
        return new Document("name",this.name).
                append("lastName",this.lastName)
                .append("username",this.username)
                .append("phone",this.phone)
                .append("email", this.email)
                .append("password", this.password)
                .append("borrowedBooks", borrowedBooks);
    }
}
