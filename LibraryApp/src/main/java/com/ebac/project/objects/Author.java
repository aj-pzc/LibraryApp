package com.ebac.project.objects;

import org.bson.Document;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

public class Author {
    private String name;
    private String lastName;
    private String biography;
    private List<Book> publishedBooks;
    private String authorKey;

    public Author(String name, String lastName, String biography) {
        this.name = name;
        this.lastName = lastName;
        this.biography = biography;
        this.publishedBooks = new ArrayList<>();

        this.authorKey = generateAuthorKey(name, lastName);
    }

    private String generateAuthorKey(String name, String lastName) {
        String parteNombre = name.length() < 3 ? name : name.substring(0, 3);
        String parteApellido = lastName.length() < 3 ? lastName : lastName.substring(0, 3);

        String key = (parteNombre + parteApellido).toUpperCase(java.util.Locale.ROOT);

        key = Normalizer.normalize(key, Normalizer.Form.NFD);
        key = key.replaceAll("[^\\p{ASCII}]", "");

        key = key.replaceAll("[^A-Z0-9-]", "");

        return key;
    }

    public String getAuthorKey() {
        return authorKey;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName;}

    public String getBiography() { return biography;}
    public void setBiography(String biography) { this.biography = biography;}

    public List<Book> getPublishedBooks() { return publishedBooks; }

    public void newBook(Book book) {
        this.publishedBooks.add(book);
    }

    public Document authorToDoc(){
        List<Document> authorBooks = this.publishedBooks
                .stream()
                .map(Book::authorBooksDoc)
                .toList();
        return new  Document ("authorKey", authorKey)
                .append("name",this.name)
                .append("lastName", this.lastName)
                .append("biography", this.biography)
                .append("publishedBooks", authorBooks);
    }


    public Document authorToBook(){
        return new Document("authorKey", authorKey)
                .append("name",this.name)
                .append("lastName", this.lastName)
                .append("biography", this.biography);
    }

    public Document authorNameDoc(){
        return new Document("authorKey", authorKey)
                .append("name",this.name)
                .append("lastName",this.lastName);
    }

    public String PrintName(){
      String lastName = this.lastName;
      String firstName = this.name;

      String fullName = lastName + ", " + firstName;
      return fullName;
    }
}