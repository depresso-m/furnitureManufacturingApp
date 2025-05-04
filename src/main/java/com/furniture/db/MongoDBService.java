package com.furniture.db;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MongoDBService {
    private static final String CONNECTION_STRING = "mongodb://localhost:27017";
    private static final String DATABASE_NAME = "furnitureManufacturing";
    
    private MongoClient mongoClient;
    private MongoDatabase database;
    
    private static MongoDBService instance;
    
    private MongoDBService() {
        try {
            mongoClient = MongoClients.create(CONNECTION_STRING);
            database = mongoClient.getDatabase(DATABASE_NAME);
            System.out.println("Connected to MongoDB successfully!");
        } catch (Exception e) {
            System.err.println("Error connecting to MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static synchronized MongoDBService getInstance() {
        if (instance == null) {
            instance = new MongoDBService();
        }
        return instance;
    }
    
    public MongoCollection<Document> getCollection(String collectionName) {
        return database.getCollection(collectionName);
    }
    
    public List<Document> getAllDocuments(String collectionName) {
        List<Document> documents = new ArrayList<>();
        try {
            MongoCollection<Document> collection = getCollection(collectionName);
            collection.find().forEach((Consumer<Document>) documents::add);
        } catch (Exception e) {
            System.err.println("Error getting all documents: " + e.getMessage());
        }
        return documents;
    }
    
    public Document getDocumentById(String collectionName, ObjectId id) {
        try {
            MongoCollection<Document> collection = getCollection(collectionName);
            return collection.find(Filters.eq("_id", id)).first();
        } catch (Exception e) {
            System.err.println("Error getting document by ID: " + e.getMessage());
            return null;
        }
    }
    
    public List<Document> findDocuments(String collectionName, Bson filter) {
        List<Document> documents = new ArrayList<>();
        try {
            MongoCollection<Document> collection = getCollection(collectionName);
            collection.find(filter).forEach((Consumer<Document>) documents::add);
        } catch (Exception e) {
            System.err.println("Error finding documents: " + e.getMessage());
        }
        return documents;
    }
    
    public List<Document> getSortedDocuments(String collectionName, String sortField, boolean ascending) {
        List<Document> documents = new ArrayList<>();
        try {
            MongoCollection<Document> collection = getCollection(collectionName);
            Bson sort = ascending ? Sorts.ascending(sortField) : Sorts.descending(sortField);
            collection.find().sort(sort).forEach((Consumer<Document>) documents::add);
        } catch (Exception e) {
            System.err.println("Error getting sorted documents: " + e.getMessage());
        }
        return documents;
    }
    
    public boolean insertDocument(String collectionName, Document document) {
        try {
            MongoCollection<Document> collection = getCollection(collectionName);
            InsertOneResult result = collection.insertOne(document);
            return result.wasAcknowledged();
        } catch (Exception e) {
            System.err.println("Error inserting document: " + e.getMessage());
            return false;
        }
    }
    
    public boolean deleteDocument(String collectionName, ObjectId id) {
        try {
            MongoCollection<Document> collection = getCollection(collectionName);
            DeleteResult result = collection.deleteOne(Filters.eq("_id", id));
            return result.getDeletedCount() > 0;
        } catch (Exception e) {
            System.err.println("Error deleting document: " + e.getMessage());
            return false;
        }
    }
    
    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
            System.out.println("MongoDB connection closed.");
        }
    }
} 