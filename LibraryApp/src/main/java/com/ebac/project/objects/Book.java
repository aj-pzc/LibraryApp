package com.ebac.project.objects;

import org.bson.Document;

public class    Book {
    private final String title;
    private final Author author;
    private final Integer year;
    private final String ISBN;
    private int totalCopies;
    private int availableCopies;


    public Book(String title, Author author, Integer year, String ISBN, int totalCopies) {
        this.title = title;
        this.author = author;
        this.year = year;
        this.ISBN = ISBN;
        this.totalCopies = totalCopies;
        this.availableCopies = totalCopies;
    }

    public String getTitle() {return title;}
    public Author getAuthor() { return author;}
    public Integer getYear() { return year;}
    public String getISBN() { return ISBN;}

    public int getTotalCopies() {return totalCopies;}
    public int getAvailableCopies() {return availableCopies;}
    public void setAvailableCopies(int availableCopies) {this.availableCopies = availableCopies;}
    public void setTotalCopies(int totalCopies) {this.totalCopies = totalCopies;}

    public Document bookToDoc(){
        return new Document("ISBN", this.ISBN).append("title", this.title).append("year", this.year).append("author", this.author.authorToBook()).append("totalCopies",this.totalCopies).append("availableCopies", this.availableCopies);
    }

    public Document authorBooksDoc(){
        return new Document("ISBN", this.ISBN).append("year", this.year).append("title", this.title);
    }

    public Document borrowedBooksDoc(){
        return new Document("ISBN", this.ISBN).append("title", this.title).append("authorName",this.author.authorNameDoc());
    }

}
