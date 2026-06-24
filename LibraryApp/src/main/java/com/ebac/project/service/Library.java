package com.ebac.project.service;
import com.ebac.project.exceptions.LibroNoDisponible;
import com.ebac.project.exceptions.UsuarioNoRegistrado;
import com.ebac.project.objects.Book;
import com.ebac.project.objects.Users;
import com.ebac.project.objects.Author;
import com.ebac.project.exceptions.ValorDuplicado;
import com.ebac.project.objects.model.AuthorModel;
import com.ebac.project.objects.model.BookModel;
import com.ebac.project.objects.model.UserModel;
import org.bson.Document;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

public class Library {
    private final BookModel booksCollection;
    private final AuthorModel authorsList;
    private final UserModel usersList;

    public Library(BookModel booksCollection, AuthorModel authorsList, UserModel usersList) {
        this.booksCollection = booksCollection;
        this.authorsList = authorsList;
        this.usersList = usersList;
    }

    //Books
    public Optional<Document> findBookByISBN(String isbn) {
        return this.booksCollection.buscarPorISBN(isbn);
    }

    public List<Document> searchBooksByTitle(String keyword) {
        Document filtro = new Document("title", new Document("$regex", keyword).append("$options", "i"));
        return this.booksCollection.getCollection().find(filtro).into(new ArrayList<>());
    }

    public long countBooksPublishedByYear(int year) {
        Document filtro = new Document("year", year);
        return this.booksCollection.getCollection().countDocuments(filtro);
    }

    public void addBook(Book book) {
        if (this.booksCollection.buscarPorISBN(book.getISBN()).isPresent()) {
            throw new ValorDuplicado("Error de inventario: El libro con ISBN " + book.getISBN() + " ya está registrado.");
        }
        this.booksCollection.guardar(book);
    }

    public boolean matchByWord(String keyword) {
        Document filtro = new Document("biography", new Document("$regex", keyword).append("$options", "i"));
        return this.authorsList.getCollection().countDocuments(filtro) > 0;
    }

    public void addAuthor(Author author) {
        if (this.authorsList.buscarPorKey(author.getAuthorKey()).isPresent()) {
            throw new ValorDuplicado("No se pudo registrar: El Author '" + author.PrintName() + "' ya existe.");
        }
        this.authorsList.guardar(author);
    }

    //Users
    public void registerUser(Users user) {
        if (this.usersList.buscarPorUsername(user.getUsername()).isPresent()) {
            throw new ValorDuplicado("No se pudo registrar: El nombre de usuario '" + user.getUsername() + "' ya está en uso.");
        }
        this.usersList.guardar(user);
    }

    public List<Document> getBooksBorrowedByUser(String username) {
        Optional<Document> userDoc = this.usersList.buscarPorUsername(username);
        if (userDoc.isPresent()) {
            List<Document> borrowed = (List<Document>) userDoc.get().get("borrowedBooks");
            return borrowed != null ? borrowed : Collections.emptyList();
        }
        return Collections.emptyList();
    }

    public void loanBook(String isbn, String username) throws LibroNoDisponible, UsuarioNoRegistrado {
        this.usersList.buscarPorUsername(username)
                .orElseThrow(() -> new UsuarioNoRegistrado("Usuario no encontrado: " + username));

        Document bookDoc = this.booksCollection.buscarPorISBN(isbn)
                .orElseThrow(() -> new LibroNoDisponible("El libro no existe en el catálogo."));

        int disponibles = bookDoc.getInteger("availableCopies");
        if (disponibles <= 0) {
            throw new LibroNoDisponible("Lo sentimos, no quedan unidades disponibles de: " + bookDoc.getString("title"));
        }

        Document filtroLibro = new Document("ISBN", isbn);
        Document restarCopia = new Document("$inc", new Document("availableCopies", -1));
        this.booksCollection.getCollection().updateOne(filtroLibro, restarCopia);

        Document libroResumido = new Document("ISBN", isbn).append("title", bookDoc.getString("title"));
        Document operacionPush = new Document("$push", new Document("borrowedBooks", libroResumido));
        this.usersList.getCollection().updateOne(new Document("username", username), operacionPush);

        System.out.printf("Préstamo exitoso de '%s' a %s. (Copias restantes: %d)\n",
                bookDoc.getString("title"), username, (disponibles - 1));
    }

    public void returnBook(String isbn, String username) throws LibroNoDisponible, UsuarioNoRegistrado {
        Document userDoc = this.usersList.buscarPorUsername(username)
                .orElseThrow(() -> new UsuarioNoRegistrado("El usuario " + username + " no está registrado."));

        List<Document> borrowed = (List<Document>) userDoc.get("borrowedBooks");
        boolean loTiene = borrowed != null && borrowed.stream().anyMatch(b -> b.getString("ISBN").equals(isbn));

        if (!loTiene) {
            throw new LibroNoDisponible("El usuario " + username + " no tiene prestado el ISBN: " + isbn);
        }

        Document operacionPull = new Document("$pull", new Document("borrowedBooks", new Document("ISBN", isbn)));
        this.usersList.getCollection().updateOne(new Document("username", username), operacionPull);

        Document filtroLibro = new Document("ISBN", isbn);
        Document sumarCopia = new Document("$inc", new Document("availableCopies", 1));
        this.booksCollection.getCollection().updateOne(filtroLibro, sumarCopia);

        System.out.printf("Libro [%s] devuelto con éxito por %s e incorporado al inventario.\n", isbn, username);
    }

    public void removeBook(String ISBN) {

        Document bookDoc = this.booksCollection.buscarPorISBN(ISBN).orElse(null);

        if (bookDoc != null) {
            int total = bookDoc.getInteger("totalCopies");
            int disponibles = bookDoc.getInteger("availableCopies");

            if (disponibles < total) {
                System.out.printf("No se puede eliminar '%s'. Hay %d copias actualmente prestadas.\n",
                        bookDoc.getString("title"), (total - disponibles));
                return;
            }
            this.booksCollection.eliminarPorISBN(ISBN);
        } else {
            System.out.println("El libro no existe en el sistema.");
        }
    }
}