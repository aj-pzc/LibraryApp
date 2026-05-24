package com.ebac.project.objects;

public class    Book {
    private final String title;
    private final Author author;
    private final Integer year;
    private final String ISBN;

    public Book(String title, Author author, Integer year, String ISBN) {
        this.title = title;
        this.author = author;
        this.year = year;
        this.ISBN = ISBN;
    }

    public String getTitle() {return title;}
    public Author getAuthor() { return author;}
    public Integer getYear() { return year;}
    public String getISBN() { return ISBN;}
}
