package com.ebac.project.service;
import com.ebac.project.objects.Book;
import com.ebac.project.objects.User;
import com.ebac.project.objects.Author;
import com.ebac.project.exceptions.ValorDuplicado;
import com.ebac.project.exceptions.LibroNoDisponible;
import com.ebac.project.exceptions.UsuarioNoRegistrado;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Collections;

public class Library {
    private final List<Book> booksCollection;
    private final List<Author> authorsList;
    private final List<User> usersList;

    public Library() {
        this.booksCollection = new ArrayList<>();
        this.authorsList = new ArrayList<>();
        this.usersList = new ArrayList<>();
    }

    public void removeBook(Book book) {
        this.booksCollection.remove(book);
    }

    public List<Book> searchBooksByTitle(String keyword) {
        return booksCollection.stream()
                .filter(book -> book.getTitle().toLowerCase().contains(keyword.toLowerCase()))
                .toList();
    }

    public long countBooksPublishedByYear(int year) {
        return booksCollection.stream()
                .filter(book -> book.getYear() == year)
                .count();
    }

    public Book findBookByISBN(String ISBN) throws LibroNoDisponible {
        return booksCollection.stream()
                .filter(book -> book.getISBN().equals(ISBN))
                .findFirst()
                .orElseThrow(()-> new LibroNoDisponible("El libro no esta en sistema, revisa busqueda: "+ISBN));
    }

    public boolean matchByWord(String keyword) {
        return authorsList.stream()
                .anyMatch(author -> author.getBiography().toLowerCase().contains(keyword.toLowerCase()));
    }

    public List<Book> getBooksBorrowedByUser(String email) {
        return usersList.stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .map(User::getBorrowedBooks)
                .orElse(Collections.emptyList());
    }

    public void addBook(Book book) {
        boolean existingISBN = booksCollection.stream()
                .anyMatch(b -> b.getISBN().equals(book.getISBN()));

        if (existingISBN) {
            throw new ValorDuplicado("Error de inventario: El libro con ISBN " + book.getISBN() + " ya está registrado.");
        }
        this.booksCollection.add(book);
    }

    public void registerUser(User user) {
        boolean emailExists = usersList.stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(user.getEmail()));

        if (emailExists) {
            throw new ValorDuplicado("No se pudo registrar: El correo '" + user.getEmail() + "' ya está en uso.");
        }
        this.usersList.add(user);
    }

    public void addAuthor(Author author) {
        boolean exsistingAuthor = authorsList.stream()
                .anyMatch(u -> u.getName().equals(author.getName()) && u.getLastName().equals(author.getLastName()));
        if (exsistingAuthor) {
            throw new ValorDuplicado("No se pudo registrar: El Author '" + author.getName() + author.getLastName() + "' ya existe.");
        }

        this.authorsList.add(author);
    }

    public void loanBook(String ISBN, String userEmail) throws LibroNoDisponible, UsuarioNoRegistrado {
        Book book = findBookByISBN(ISBN);

        boolean isAlreadyLoaned = usersList.stream()
                .anyMatch(user -> user.getBorrowedBooks().stream()
                        .anyMatch(b -> b.getISBN().equals(ISBN)));

        if (isAlreadyLoaned) {
            throw new LibroNoDisponible("El libro '" + book.getTitle() + "' ya está prestado a otro usuario.");
        }

        User user = usersList.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(userEmail))
                .findFirst()
                .orElseThrow(() -> new UsuarioNoRegistrado("Usuario no encontrado."));

        user.borrowBook(book);
    }

    public void returnBook(String ISBN, String userEmail) throws LibroNoDisponible, UsuarioNoRegistrado {
        User user = usersList.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(userEmail))
                .findFirst()
                .orElseThrow(() -> new UsuarioNoRegistrado("El usuario con email " + userEmail + " no está registrado."));

        Book bookToReturn = user.getBorrowedBooks().stream()
                .filter(b -> b.getISBN().equals(ISBN))
                .findFirst()
                .orElseThrow(() -> new LibroNoDisponible("El usuario no tiene asignado un libro con el ISBN: " + ISBN));
        user.returnBook(bookToReturn);
    }

}
