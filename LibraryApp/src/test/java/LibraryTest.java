import com.ebac.project.exceptions.LibroNoDisponible;
import com.ebac.project.exceptions.UsuarioNoRegistrado;
import com.ebac.project.exceptions.ValorDuplicado;
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
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class LibraryTest {

    private MongoClient mongoClient;
    private MongoDatabase database;

    private BookModel bookModel;
    private AuthorModel authorModel;
    private UserModel userModel;
    private Library library;

    @BeforeEach
    public void setUp() {
        this.mongoClient = MongoClients.create("mongodb://root:root@localhost:27017/?authSource=admin");
        this.database = mongoClient.getDatabase("LibraryTestingDB");

        database.getCollection("books").drop();
        database.getCollection("authors").drop();
        database.getCollection("users").drop();

        this.bookModel = new BookModel(database);
        this.authorModel = new AuthorModel(database);
        this.userModel = new UserModel(database);

        this.library = new Library(bookModel, authorModel, userModel);
    }

    @AfterEach
    public void tearDown() {
        if (this.mongoClient != null) {
            this.mongoClient.close();
        }
    }

    @Test
    public void duplicateAddBookExceptionTest() {
        Author author = new Author("Suzanne", "Collins", "Biografía");
        Book book = new Book("The Hunger Games", author, 2008, "9780439023528", 5);
        library.addBook(book);

        assertThrows(ValorDuplicado.class, () -> library.addBook(book),
                "Debería arrojar ValorDuplicado si el ISBN ya está registrado.");
    }

    @Test
    public void  duplicateAddUserExceptionTest() {
        Users user = new Users("Aldo", "Paez","aldo@FIRSTmail.com","apzc","1234asdf");
        library.registerUser(user);
        assertThrows(ValorDuplicado.class, () -> library.registerUser(user));
    }

    @Test
    public void searchByTitleTest() {
        Author author = new Author("Suzanne", "Collins", "Biografía");
        library.addBook(new Book("The Hunger Games", author, 2008, "1111", 5));
        library.addBook(new Book("Catching Fire", author, 2009, "2222", 5));

        List<Document> resultados = library.searchBooksByTitle("games");

        assertEquals(1, resultados.size());
        assertEquals("The Hunger Games", resultados.get(0).getString("title"));
    }

    @Test
    public void loanBookTest() throws Exception {
        Users user = new Users("Aldo", "Paez","aldo@FIRSTmail.com","apzc","1234asdf");
        library.registerUser(user);

        Author author = new Author("Suzanne", "Collins", "Biografía");
        Book book = new Book("Mockingjay", author, 2010, "9780439023511", 2);
        library.addBook(book);

        library.loanBook("9780439023511", "apzc");

        Document libroEnDb = library.findBookByISBN("9780439023511").orElseThrow();
        assertEquals(1, libroEnDb.getInteger("availableCopies"));

        List<Document> prestados = library.getBooksBorrowedByUser("apzc");
        assertEquals(1, prestados.size());
        assertEquals("Mockingjay", prestados.get(0).getString("title"));
    }

    @Test
    public void loanBookExceptionTest() {
        Users user = new Users("Aldo", "Paez","aldo@FIRSTmail.com","reader2","1234asdf");

        library.registerUser(user);

        Author author = new Author("Suzanne", "Collins", "Biografía");
        Book book = new Book("Out of Stock Book", author, 2026, "9781111111111", 0);
        library.addBook(book);

        assertThrows(LibroNoDisponible.class, () -> library.loanBook("9781111111111", "reader2"));
    }

    @Test
    public void loanBookUnregisteredUserExceptionTest() {
        Author author = new Author("Suzanne", "Collins", "Biografía");
        Book book = new Book("The Hunger Games", author, 2008, "9780439023528", 5);
        library.addBook(book);

        assertThrows(UsuarioNoRegistrado.class, () -> library.loanBook("9780439023528", "notRegisteredUser"));
    }

    @Test
    public void returnBookTest () throws Exception {

        Users user = new Users("Aldo", "Paez","aldo@FIRSTmail.com","reader3","1234asdf");
        library.registerUser(user);
        Author author = new Author("Suzanne", "Collins", "Biografía");
        Book book = new Book("The Hunger Games", author, 2008, "9780439023528", 5);
        library.addBook(book);

        library.loanBook("9780439023528", "reader3"); // Baja stock a 4

        library.returnBook("9780439023528", "reader3");

        Document libroEnDb = library.findBookByISBN("9780439023528").orElseThrow();
        assertEquals(5, libroEnDb.getInteger("availableCopies"));

        List<Document> prestados = library.getBooksBorrowedByUser("reader3");
        assertTrue(prestados.isEmpty());
    }

    @Test
    public void removeBookLockedWhenBorrowedTest() throws Exception {
        Users user = new Users("Aldo", "Paez","aldo@FIRSTmail.com","reader4","1234asdf");
        library.registerUser(user);
        Author author = new Author("Suzanne", "Collins", "Biografía");
        Book book = new Book("The Hunger Games", author, 2008, "9780439023528", 1);
        library.addBook(book);

        library.loanBook("9780439023528", "reader4"); // total=1, available=0

        library.removeBook("9780439023528");

        Optional<Document> libroEnDb = library.findBookByISBN("9780439023528");
        assertTrue(libroEnDb.isPresent(), "El libro no debió borrarse porque hay unidades prestadas.");
    }
}
