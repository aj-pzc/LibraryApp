package com.ebac.project;

import com.ebac.project.exceptions.LibroNoDisponible;
import com.ebac.project.exceptions.UsuarioNoRegistrado;
import com.ebac.project.objects.Author;
import com.ebac.project.objects.Book;
import com.ebac.project.objects.User;
import com.ebac.project.service.Library;

public class Main {
    static void main(String[] args) {
            Library mylibrary = new Library();
            Author author1 = new Author(
                    "Rick",
                    "Riordan",
                    "Mithology Fiction writer best known for 'Percy Jackson and the Olympians'"
            );

            Book book1 = new Book(
                    "Percy Jackson and the Lightning Thief",
                    author1,
                    2010,
                    "9781484458754"
            );

            User user1 = new User("Aldo", "aldo@mail.com", "jasjf444asdfa");


            mylibrary.registerUser(user1);
            mylibrary.addBook(book1);

        try {
            mylibrary.loanBook("9781484458754", "aldo@mail.com");
            System.out.println("¡Préstamo realizado con éxito a " + user1.getName() + "!");

            System.out.println("Libros en poder del usuario: " + mylibrary.getBooksBorrowedByUser("aldo@mail.com").size());
            mylibrary.loanBook("9781484458754", "aldo@mail.com");

        } catch (LibroNoDisponible e) {
            System.err.println("ALERTA DE BIBLIOTECA: " + e.getMessage());

        } catch (UsuarioNoRegistrado e) {
            System.err.println("ALERTA DE USUARIO: " + e.getMessage());
        }

        System.out.println(mylibrary.getBooksBorrowedByUser("aldo@mail.com"));
    }
}


