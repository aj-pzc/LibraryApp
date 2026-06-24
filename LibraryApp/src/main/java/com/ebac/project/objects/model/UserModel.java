package com.ebac.project.objects.model;
import com.ebac.project.objects.Users;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.util.Optional;

public class UserModel {

    private final MongoCollection<Document> collection;

    public UserModel(MongoDatabase database) {
        collection = database.getCollection("users");
        this.collection.createIndex(new Document("username", 1), new IndexOptions().unique(true));

    }

    public Optional<Document> buscarPorUsername(String username){
        Document result = this.collection.find(new Document("username",username)).first();
        return Optional.ofNullable(result);
    }


    public void guardar (Users user){
        if (buscarPorUsername(user.getUsername()).isPresent()){
            System.out.printf("El usuario:   \n %s\nya esta ocupado, use otro usario.",user.getUsername());
            return;
        }
        Document userDoc = user.userToDoc();
        this.collection.insertOne(userDoc);
        System.out.printf("Usario:\n   %s\nregistrado con exito!\n", user.getUsername());
    }

    public Optional<Document> buscarPorId(String idString){
        try {
            ObjectId idDoc = new ObjectId(idString);
            Document result = collection.find( new Document("_id", idDoc)).first();
            return Optional.ofNullable(result);

        } catch (IllegalArgumentException e){
            return Optional.empty();
        }
    }

    public void eliminarPorId(String idString){
        if (buscarPorId(idString).isPresent()){
            collection.deleteOne( new Document("_id", new ObjectId(idString)));
            System.out.println("Eliminado con exito!");
        } else {
            System.out.println("El ID no existe");
        }
    }

    public void actualizarPorId(String idString, Users updateUser){
        if (buscarPorId(idString).isPresent()){
            Document filter = new Document("_id", new ObjectId(idString));
            Document updatedEntry = new Document("$set", updateUser.userToDoc());

            collection.updateOne(filter, updatedEntry);
            System.out.println("Actualizado con exito!");
        } else {
            System.out.println("ID no encontrado");
        }
    }


    public void eliminarPorUsername(String username){
        if (buscarPorUsername(username).isPresent()){
            collection.deleteOne( new Document("username", username));
            System.out.println("Eliminado con exito!");
        } else {
            System.out.println("El user no existe");
        }
    }

    public void actualizarPorUsername(String username, Users updateUser){
        if (buscarPorUsername(username).isPresent()){
            Document filter = new Document("username", username);
            Document updatedEntry = new Document("$set", updateUser.userToDoc());
            collection.updateOne(filter, updatedEntry);
            System.out.println("Actualizado con exito!");
        } else {
            System.out.println("ID no encontrado");
        }
    }

    public MongoCollection<Document> getCollection() {
        return this.collection;
    }
}