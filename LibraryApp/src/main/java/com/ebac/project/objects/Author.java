package com.ebac.project.objects;

import java.util.ArrayList;
import java.util.List;

public class Author {
    private String name;
    private String lastName;
    private String biography;
    private List<Book> publishedBooks;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName;}

    public String getBiography() { return biography;}
    public void setBiography(String biography) { this.biography = biography;}

    public Author(String name, String lastName, String biography) {
        this.name = name;
        this.lastName = lastName;
        this.biography = biography;
        this.publishedBooks = new ArrayList<>();
    }

    public List<Book> getPublishedBooks() { return publishedBooks; }
    public void newBook(Book book) {
        this.publishedBooks.add(book);
    }

}
