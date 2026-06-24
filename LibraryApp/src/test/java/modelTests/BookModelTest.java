package modelTests;
import com.ebac.project.objects.Author;
import com.ebac.project.objects.Book;
import com.ebac.project.objects.model.BookModel;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static com.mongodb.assertions.Assertions.assertFalse;
import static com.mongodb.assertions.Assertions.assertNotNull;
import static com.mongodb.internal.connection.tlschannel.util.Util.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BookModelTest {
    private MongoClient mongoClient;
    private MongoDatabase database;
    private BookModel bookModel;

    @BeforeEach
    public void setUp() {
        this.mongoClient = MongoClients.create("mongodb://root:root@localhost:27017/?authSource=admin");
        this.database = mongoClient.getDatabase("LibraryTestingDB");

        database.getCollection("books").drop();
        database.getCollection("authors").drop();

        this.bookModel = new BookModel(database);
    }

    @AfterEach
    public void tearDown() {
        if (this.mongoClient != null) {
            this.mongoClient.close();
        }
    }

    @Test
    public void saveEntryUnderBooksAndAuthorTest() {
        Author author = new Author("Suzanne", "Collins", "Dystopian Author");
        Book book = new Book("The Hunger Games", author, 2008, "9780439023528", 10);
        bookModel.guardar(book);

        Optional<Document> libroDocEntry = bookModel.buscarPorISBN("9780439023528");
        assertTrue(libroDocEntry.isPresent());
        assertEquals("The Hunger Games", libroDocEntry.get().getString("title"));

        Document autorDoc = database.getCollection("authors")
                .find(new Document("authorKey", "SUZCOL")).first();

        assertNotNull(autorDoc);
        assertEquals("Suzanne", autorDoc.getString("name"));

        List<Document> publishedBooks = (List<Document>) autorDoc.get("publishedBooks");
        assertNotNull(publishedBooks);
        assertEquals(1, publishedBooks.size());
        assertEquals("9780439023528", publishedBooks.get(0).getString("ISBN"));
    }

    @Test
    public void duplicateBookTest() {
        Author author = new Author("Suzanne", "Collins", "Dystopian Author");
        Book book1 = new Book("The Hunger Games", author, 2008, "9780439023528", 10);
        bookModel.guardar(book1);

        Book book2 = new Book("The Hunger Games - Alternative Edition", author, 2008, "9780439023528", 5);
        bookModel.guardar(book2);

        long totalLibros = database.getCollection("books").countDocuments();
        assertEquals(1, totalLibros);

        Document docEnDb = bookModel.buscarPorISBN("9780439023528").orElseThrow();
        assertEquals("The Hunger Games", docEnDb.getString("title"));
    }

    @Test
    public void onDeleteRemoveBothEntriesTest() {
        Author author = new Author("Suzanne", "Collins", "Dystopian Author");
        Book book = new Book("The Hunger Games", author, 2008, "9780439023528", 10);
        bookModel.guardar(book);

        bookModel.eliminarPorISBN("9780439023528");

        assertFalse(bookModel.buscarPorISBN("9780439023528").isPresent());

        Document autorDoc = database.getCollection("authors")
                .find(new Document("authorKey", "SUZCOL")).first();
        assertNotNull(autorDoc);

        List<Document> publishedBooks = (List<Document>) autorDoc.get("publishedBooks");
        assertTrue(publishedBooks.isEmpty());
    }

    @Test
    public void modifyBookByISBNTest() {
        Author author = new Author("Suzanne", "Collins", "Dystopian Author");
        Book bookOriginal = new Book("The Hunger Games", author, 2008, "9780439023528", 10);
        bookModel.guardar(bookOriginal);

        Book bookModificado = new Book("The Hunger Games (Special Edition)", author, 2009, "9780439023528", 15);

        bookModel.actualizarPorISBN("9780439023528", bookModificado);

        Document docActualizado = bookModel.buscarPorISBN("9780439023528").orElseThrow();
        assertEquals("The Hunger Games (Special Edition)", docActualizado.getString("title"));
        assertEquals(2009, docActualizado.getInteger("year"));
    }
}
