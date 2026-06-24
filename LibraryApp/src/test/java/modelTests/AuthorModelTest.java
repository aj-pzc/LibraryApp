package modelTests;

import com.ebac.project.objects.Author;
import com.ebac.project.objects.model.AuthorModel;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class AuthorModelTest {
    private MongoClient mongoClient;
    private MongoDatabase database;
    private AuthorModel authorModel;

    @BeforeEach
    public void setUp() {
        this.mongoClient = MongoClients.create("mongodb://root:root@localhost:27017/");
        this.database = mongoClient.getDatabase("LibraryTests");

        database.getCollection("authors").drop();

        this.authorModel = new AuthorModel(database);
    }

    @AfterEach
    public void tearDown() {
        if(this.mongoClient != null) {
            this.mongoClient.close();
        }
    }

    @Test
    public void searchByIdTest(){
        Document mockAuthor = new Document("authorKey", "SUZCOL")
                .append("name", "Suzanne")
                .append("last", "Collins")
                .append("biography", "Dystopian Writer");
        database.getCollection("authors").insertOne(mockAuthor);

        Optional <Document> resultadoDoc = authorModel.buscarPorKey("SUZCOL");


        assertTrue(resultadoDoc.isPresent(), "El autor deberia estar presente");
        Document resultado = (Document) resultadoDoc.get();

        assertEquals("Suzanne", resultado.getString("name"));
        assertEquals("Collins", resultado.getString("last"));
    }

    @Test
    public void searchByKeyTest_returnEmpty(){
        Optional<Document> resultadoDoc = authorModel.buscarPorKey("SUZCOLL");
        assertFalse(resultadoDoc.isPresent(), "El Optional deberia estar vacio");
    }

    @Test
    public void searchByFullNameTest (){
        Document mockAuthor = new Document("authorKey", "SUZCOL")
                .append("name", "Suzanne")
                .append("lastName", "Collins");
        database.getCollection("authors").insertOne(mockAuthor);

        Optional<Document> resultadoDoc = authorModel.buscarPorNombreCompleto("Suzanne", "Collins");

        assertTrue(resultadoDoc.isPresent());

        assertEquals("SUZCOL",resultadoDoc.get().getString("authorKey"));
    }

    @Test
    public void saveAuthorSuccessfullyTest (){
        Author newAuthor = new Author("Suzanne", "Collins", "Biography text");

        authorModel.guardar(newAuthor);

        Document docInDB = database.getCollection("authors").
                find(new Document("authorKey", newAuthor.getAuthorKey())).first();

        assertNotNull(docInDB, "Author deberia ser guardado en DB");
        assertEquals("Collins", docInDB.getString("lastName"));
    }

    @Test
    public void duplicateAuthorTest_returnError (){
        Author autorOriginal = new Author("Suzanne", "Collins", "Biografía original");
        authorModel.guardar(autorOriginal);

        Author autorDuplicado = new Author("Suzanne", "Collins", "Biografía modificada que no debería cambiar");
        authorModel.guardar(autorDuplicado);

        long totalAutores = database.getCollection("authors").countDocuments();
        assertEquals(1, totalAutores, "La colección solo debe albergar un registro.");

        Document docEnBd = database.getCollection("authors")
                .find(new Document("authorKey", "SUZCOL")).first();
        assertNotNull(docEnBd);
        assertEquals("Biografía original", docEnBd.getString("biography"), "El registro original no debió alterarse.");
    }

    @Test
    public void deleteByKeyTest() {
        Document mockAuthor = new Document("authorKey", "SUZCOL").append("name", "Suzanne");
        database.getCollection("authors").insertOne(mockAuthor);

        authorModel.eliminarPorKey("SUZCOL");

        long conteo = database.getCollection("authors").countDocuments(new Document("authorKey", "SUZCOL"));
        assertEquals(0, conteo, "El autor debió borrarse por completo.");
    }

    @Test
    public void updateByKeyTest() {
        Document mockAuthor = new Document("authorKey", "SUZCOL")
                .append("name", "Suzanne")
                .append("lastName", "Collins")
                .append("biography", "Biografía vieja");
        database.getCollection("authors").insertOne(mockAuthor);

        Author authorActualizado = new Author("Suzanne", "Collins", "Biografía totalmente renovada 2026");

        authorModel.actualizarPorKey("SUZCOL", authorActualizado);

        Document docEnBd = database.getCollection("authors")
                .find(new Document("authorKey", "SUZCOL")).first();

        assertNotNull(docEnBd);
        assertEquals("Biografía totalmente renovada 2026", docEnBd.getString("biography"), "La biografía debió actualizarse.");
        assertEquals("Suzanne", docEnBd.getString("name"), "El nombre debe permanecer igual.");
    }
}
