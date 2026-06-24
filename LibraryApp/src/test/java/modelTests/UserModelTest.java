package modelTests;

import com.ebac.project.objects.Users;
import com.ebac.project.objects.model.UserModel;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


public class UserModelTest {
    private MongoClient mongoClient;
    private MongoDatabase database;
    private UserModel userModel;

    @BeforeEach
    public void setUp() {
        this.mongoClient = MongoClients.create("mongodb://root:root@localhost:27017/?authSource=admin");
        this.database = mongoClient.getDatabase("LibraryTestingDB");

        database.getCollection("users").drop();
        this.userModel = new UserModel(database);
    }

    @AfterEach
    public void tearDown() {
        if (this.mongoClient != null) {
            this.mongoClient.close();
        }
    }

    @Test
    public  void findByUsernameReturnsUserTest(){
        Document mockUser = new Document("username", "aldopz")
                .append("email", "aldopz@mail.com")
                .append("borrowedBooks", Collections.emptyList());
        database.getCollection("users").insertOne(mockUser);

        Optional<Document> searchResult= userModel.buscarPorUsername("aldopz");

        assertTrue(searchResult.isPresent());
        assertEquals("aldopz@mail.com", searchResult.get().getString("email"));
    }

    @Test
    public  void findByInvalidUsernameReturnsError(){
        Optional<Document> searchResult = userModel.buscarPorId("id-invalido-123");
        assertFalse(searchResult.isPresent(), "Debe retornar Optional.empty() ante un ID mal estructurado.");
    }

    @Test
    public void findByIdReturnsUserTest(){
        ObjectId newId = new ObjectId();
        Document mockUser = new Document("_id", newId)
                .append("username", "pzccaldo");
        database.getCollection("users").insertOne(mockUser);

        Optional<Document> searchResult = userModel.buscarPorId(newId.toHexString());

        assertTrue(searchResult.isPresent());
        assertEquals("pzccaldo", searchResult.get().getString("username"));
    }

    @Test
    public void saveUserSuccessfullyTest(){
        Users nuevoUsuario = new Users("Aldo", "Paez","aldo@mail.com","apzc","1234asdf");
        userModel.guardar(nuevoUsuario);

        Document docEnDb = database.getCollection("users")
                .find(new Document("username", "apzc")).first();

        assertNotNull(docEnDb, "El usuario debió guardarse con éxito.");
        assertEquals("aldo@mail.com", docEnDb.getString("email"));
    }

    @Test
    public void saveDuplicateUserReturnsError(){

        Users nuevoUsuario = new Users("Aldo", "Paez","aldo@FIRSTmail.com","apzc","1234asdf");
        userModel.guardar(nuevoUsuario);

        Users nuevoUsuarioDuplicado = new Users("Aldo", "Paez","aldo@mail.com","apzc","1234asdf");
        userModel.guardar(nuevoUsuarioDuplicado);


        long conteoTotal = database.getCollection("users").countDocuments();
        assertEquals(1, conteoTotal, "El índice o validación impidió la duplicidad.");

        Document docEnDb = database.getCollection("users")
                .find(new Document("username", "apzc")).first();
        assertNotNull(docEnDb);
        assertEquals("aldo@FIRSTmail.com", docEnDb.getString("email"), "El correo original se mantuvo intacto.");
    }

    @Test
    public void updateUserSuccessfullyTest(){
        Document mockUser = new Document("username", "apzc")
                .append("email", "aldo@mail.com");
        database.getCollection("users").insertOne(mockUser);

        Users updatedMockUser = new Users("aldo","paez","aldo@newmail.com","apzc", "password");

        userModel.actualizarPorUsername("apzc",updatedMockUser);

        Document actualizado = database.getCollection("users")
                .find(new Document("username", "apzc")).first();
        assertNotNull(actualizado);
        assertEquals("aldo@newmail.com", actualizado.getString("email"), "El correo debió ser reemplazado.");
    }

    @Test
    public void deleteUserSuccessfullyTest(){
        Document mockUser = new Document("username", "apzc")
                .append("email", "aldo@mail.com");
        database.getCollection("users").insertOne(mockUser);

        userModel.eliminarPorUsername("apzc");

        long conteoPostBorrado = database.getCollection("users")
                .countDocuments(new Document("username", "apzc"));
        assertEquals(0, conteoPostBorrado, "El usuario debió eliminarse por completo.");
    }
}
