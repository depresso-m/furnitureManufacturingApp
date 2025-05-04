package com.furniture.repository;

import com.furniture.db.MongoDBService;
import com.furniture.model.Furniture;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class FurnitureRepository {
    private static final String COLLECTION_NAME = "furniture";
    private final MongoDBService mongoDBService;
    
    public FurnitureRepository() {
        this.mongoDBService = MongoDBService.getInstance();
    }
    
    public List<Furniture> getAllFurniture() {
        List<Furniture> furnitureList = new ArrayList<>();
        List<Document> documents = mongoDBService.getAllDocuments(COLLECTION_NAME);
        
        for (Document doc : documents) {
            furnitureList.add(documentToFurniture(doc));
        }
        
        return furnitureList;
    }
    
    public Furniture getFurnitureById(ObjectId id) {
        Document doc = mongoDBService.getDocumentById(COLLECTION_NAME, id);
        return doc != null ? documentToFurniture(doc) : null;
    }
    
    public List<Furniture> findFurnitureByName(String name) {
        Pattern pattern = Pattern.compile(name, Pattern.CASE_INSENSITIVE);
        Bson filter = Filters.regex("name", pattern);
        return findFurniture(filter);
    }
    
    public List<Furniture> findFurnitureByCategory(String category) {
        Bson filter = Filters.eq("category", category);
        return findFurniture(filter);
    }
    
    public List<Furniture> getSortedFurniture(String sortField, boolean ascending) {
        List<Furniture> furnitureList = new ArrayList<>();
        List<Document> documents = mongoDBService.getSortedDocuments(COLLECTION_NAME, sortField, ascending);
        
        for (Document doc : documents) {
            furnitureList.add(documentToFurniture(doc));
        }
        
        return furnitureList;
    }
    
    private List<Furniture> findFurniture(Bson filter) {
        List<Furniture> furnitureList = new ArrayList<>();
        List<Document> documents = mongoDBService.findDocuments(COLLECTION_NAME, filter);
        
        for (Document doc : documents) {
            furnitureList.add(documentToFurniture(doc));
        }
        
        return furnitureList;
    }
    
    public boolean addFurniture(Furniture furniture) {
        Document doc = furnitureToDocument(furniture);
        return mongoDBService.insertDocument(COLLECTION_NAME, doc);
    }
    
    public boolean deleteFurniture(ObjectId id) {
        return mongoDBService.deleteDocument(COLLECTION_NAME, id);
    }
    
    private Furniture documentToFurniture(Document doc) {
        Furniture furniture = new Furniture();
        furniture.setId(doc.getObjectId("_id"));
        furniture.setName(doc.getString("name"));
        furniture.setCategory(doc.getString("category"));
        
        Object priceObj = doc.get("price");
        double price = 0.0;
        if (priceObj instanceof Integer) {
            price = ((Integer) priceObj).doubleValue();
        } else if (priceObj instanceof Double) {
            price = (Double) priceObj;
        }
        furniture.setPrice(price);
        
        furniture.setStockQuantity(doc.getInteger("stockQuantity"));
        return furniture;
    }
    
    private Document furnitureToDocument(Furniture furniture) {
        Document doc = new Document();
        if (furniture.getId() != null) {
            doc.append("_id", furniture.getId());
        }
        doc.append("name", furniture.getName())
           .append("category", furniture.getCategory())
           .append("price", furniture.getPrice())
           .append("stockQuantity", furniture.getStockQuantity());
        return doc;
    }
} 