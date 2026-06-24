package com.ebac.project.objects.model;
import com.ebac.project.objects.Book;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import java.util.Optional;

public class BookModel {
    private final MongoCollection<Document> collection;
    private final MongoCollection<Document>  authorCollection;

    public BookModel(MongoDatabase database) {
        this.collection = database.getCollection("books");
        this.authorCollection = database.getCollection("authors");

        this.collection.createIndex(new Document("ISBN", 1), new IndexOptions().unique(true));
    }

    public Optional<Document> buscarPorISBN(String ISBNString){
            Document result = collection.find( new Document("ISBN", ISBNString)).first();
            return Optional.ofNullable(result);
    }

    public void guardar (Book book){

        if (buscarPorISBN(book.getISBN()).isPresent()){
            System.out.printf("La edicion [%s] del libro %s ya esta registrada.",book.getISBN(),book.getTitle());
            return;
        }
        Document bookDoc = book.bookToDoc();
        this.collection.insertOne(bookDoc);


        String authorKey = book.getAuthor().getAuthorKey();
        Document authorBookEntry = book.authorBooksDoc();

        Document filtroAuthor = new Document("authorKey", authorKey);

        Document authorBookEntryDoc = new Document("authorKey", authorKey)
                .append("name", book.getAuthor().getName())
                .append("lastName", book.getAuthor().getLastName())
                .append("biography",  book.getAuthor().getBiography());

        Document pushBook = new Document("$addToSet",new Document("publishedBooks", authorBookEntry));
            pushBook.append("$setOnInsert",authorBookEntryDoc);

        UpdateOptions addOptional = new UpdateOptions().upsert(true);

        this.authorCollection.updateOne(filtroAuthor,pushBook, addOptional);
        System.out.printf("Libro: \n   %s\nguardado con exito!\n", book.getTitle());
    }

    public void eliminarPorISBN(String ISBN){
        Document libroDoc = buscarPorISBN(ISBN).orElse(null);

        if (libroDoc != null) {
            Document autorDoc = (Document) libroDoc.get("author");

            collection.deleteOne(new Document("ISBN", ISBN));
            if (autorDoc != null) {

                Document filtroAutor = new Document("authorKey", autorDoc.get("authorKey"));

                Document operacionPull = new Document("$pull", new Document("publishedBooks", new Document("ISBN", ISBN)));
                this.authorCollection.updateOne(filtroAutor, operacionPull);
            }

            System.out.println("¡Libro eliminado de la biblioteca y del catálogo del autor con éxito!");
        } else {
            System.out.println("El ISBN no existe.");
        }
    }

    public void actualizarPorISBN(String ISBN, Book bookUpdate){
        if (buscarPorISBN(ISBN).isPresent()){
            Document filter = new Document("ISBN", ISBN);
            Document updatedEntry = new Document("$set", bookUpdate.bookToDoc());

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