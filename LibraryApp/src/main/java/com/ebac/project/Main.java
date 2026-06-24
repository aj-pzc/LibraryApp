package com.ebac.project;

import com.ebac.project.exceptions.LibroNoDisponible;
import com.ebac.project.exceptions.UsuarioNoRegistrado;
import com.ebac.project.objects.Author;
import com.ebac.project.objects.Book;
import com.ebac.project.objects.Users;
import com.ebac.project.objects.model.AuthorModel;
import com.ebac.project.objects.model.BookModel;
import com.ebac.project.objects.model.UserModel;
import com.ebac.project.service.Library;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class Main {
    static void main(String[] args) throws UsuarioNoRegistrado, LibroNoDisponible {

        String connectionString = "mongodb://root:root@localhost:27017/";
        MongoClient mongoClient = MongoClients.create(connectionString);
        MongoDatabase database = mongoClient.getDatabase("LibraryProject");

        AuthorModel authors = new AuthorModel(database);
        BookModel books = new BookModel(database);
        UserModel users = new UserModel(database);
        Library mylibrary = new Library(books, authors,users);


        Author author1 = new Author(
                "Rick",
                "Riordan",
                "Mithology Fiction writer best known for 'Percy Jackson and the Olympians'"
        );
        Author author2 = new Author(
                "Suzane",
                "Collins",
                "American author and television writer who is best known as the author of the young adult dystopian fiction book series The Hunger Games"

        );
        Author author2Updated = new Author(
                "Suzanne",
                "Collins",
                "American author best known as the author of the young adult dystopian fiction book series The Hunger Games"
        );

        Book book1 = new Book(
                "Percy Jackson and the Lightning Thief",
                author1,
                2010,
                "9781484458754",
                3
        );
        Book book2 = new Book("Percy Jackson and the Sea of Monsters",
                author1,
                2011,
                "9781484447352",
                1
        );
        Book book3 = new Book(
                "The Hunger Games: Sunrise on the Reaping",
                author2,
                2025,
                "9781546171461",
                10
        );

        Users user1 = new Users("Aldo", "Paez",  "aldo@mail.com",  "aldopz","jasjf444asdfa");

        Users user2 = new Users("Aldo", "Paez",  "aldo@mail.com",  "aldopcz","jasjf444asdfa");

        /**/
        users.guardar(user1);
        mylibrary.registerUser(user2);

        mylibrary.addBook(book1);
        mylibrary.addBook(book2);

        authors.guardar(author2);
        authors.actualizarPorKey("SUZCOL",author2Updated);

        mylibrary.addBook(book3);

        mylibrary.loanBook("9781484447352","aldopz");
        mylibrary.loanBook("9781546171461","aldopz");
        mylibrary.loanBook("9781484458754","aldopz");

        mylibrary.removeBook("9781546171461");
        mylibrary.returnBook("9781484447352","aldopz");
    }
}


