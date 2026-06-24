package com.ebac.project.objects.model;
import com.ebac.project.objects.Author;
import com.mongodb.client.model.IndexOptions;
import org.bson.Document;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.types.ObjectId;

import java.util.Optional;

public class AuthorModel {
    private final MongoCollection<Document> collection;

    public AuthorModel(MongoDatabase database) {
        this.collection = database.getCollection("authors");
        this.collection.createIndex(new Document("authorKey",1), new IndexOptions().unique(true));
    }

    public Optional<Document> buscarPorKey(String authorCode){
            Document result = collection.find( new Document("authorKey", authorCode)).first();
            return Optional.ofNullable(result);
    }

    public void guardar (Author author){
        if (buscarPorKey(author.getAuthorKey()).isPresent()) {
            System.out.printf("El autor con el código [%s] ya existe en el sistema.\n", author.getAuthorKey());
            return;
        }

        Document authorDoc = author.authorToDoc();
        this.collection.insertOne(authorDoc);
        System.out.printf("Se a agregado autor:\n   %s\ncon exito!\n", author.PrintName());
    }

    public void eliminarPorKey(String authorKey){
        if (buscarPorKey(authorKey).isPresent()){
            collection.deleteOne( new Document("authorKey", authorKey));
            System.out.println("Eliminado con exito!");
        } else {
            System.out.println("El ID no existe");
        }
    }

    public void actualizarPorKey(String authorKey, Author authorUpdate){
       if (buscarPorKey(authorKey).isPresent()){
           Document filter = new Document("authorKey", authorKey);
           Document updatedEntry = new Document("$set", authorUpdate.authorToDoc());

           collection.updateOne(filter, updatedEntry);
           System.out.println("Actualizado con exito!");
       } else {
           System.out.println("ID no encontrado");
       }
    }

    public Optional<Document> buscarPorNombreCompleto(String name, String lastName) {
        Document filtro = new Document("name", name).append("lastName", lastName);
        Document result = collection.find(filtro).first();
        return Optional.ofNullable(result);
    }

    public MongoCollection<Document> getCollection() {
        return this.collection;
    }
}