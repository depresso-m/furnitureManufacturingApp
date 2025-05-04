package com.furniture.repository;

import com.furniture.db.MongoDBService;
import com.furniture.model.Material;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MaterialRepository {
    private static final String COLLECTION_NAME = "materials";
    private final MongoDBService mongoDBService;
    
    public MaterialRepository() {
        this.mongoDBService = MongoDBService.getInstance();
    }
    
    public List<Material> getAllMaterials() {
        List<Material> materials = new ArrayList<>();
        List<Document> documents = mongoDBService.getAllDocuments(COLLECTION_NAME);
        
        for (Document doc : documents) {
            materials.add(documentToMaterial(doc));
        }
        
        return materials;
    }
    
    public Material getMaterialById(ObjectId id) {
        Document doc = mongoDBService.getDocumentById(COLLECTION_NAME, id);
        return doc != null ? documentToMaterial(doc) : null;
    }
    
    public List<Material> findMaterialsByName(String name) {
        Pattern pattern = Pattern.compile(name, Pattern.CASE_INSENSITIVE);
        Bson filter = Filters.regex("name", pattern);
        return findMaterials(filter);
    }
    
    public List<Material> findMaterialsByType(String type) {
        Bson filter = Filters.eq("type", type);
        return findMaterials(filter);
    }
    
    public List<Material> getSortedMaterials(String sortField, boolean ascending) {
        List<Material> materials = new ArrayList<>();
        List<Document> documents = mongoDBService.getSortedDocuments(COLLECTION_NAME, sortField, ascending);
        
        for (Document doc : documents) {
            materials.add(documentToMaterial(doc));
        }
        
        return materials;
    }
    
    private List<Material> findMaterials(Bson filter) {
        List<Material> materials = new ArrayList<>();
        List<Document> documents = mongoDBService.findDocuments(COLLECTION_NAME, filter);
        
        for (Document doc : documents) {
            materials.add(documentToMaterial(doc));
        }
        
        return materials;
    }
    
    public boolean addMaterial(Material material) {
        Document doc = materialToDocument(material);
        return mongoDBService.insertDocument(COLLECTION_NAME, doc);
    }
    
    public boolean deleteMaterial(ObjectId id) {
        return mongoDBService.deleteDocument(COLLECTION_NAME, id);
    }
    
    private Material documentToMaterial(Document doc) {
        Material material = new Material();
        material.setId(doc.getObjectId("_id"));
        material.setName(doc.getString("name"));
        material.setType(doc.getString("type"));
        
        Object costObj = doc.get("costPerUnit");
        double cost = 0.0;
        if (costObj instanceof Integer) {
            cost = ((Integer) costObj).doubleValue();
        } else if (costObj instanceof Double) {
            cost = (Double) costObj;
        }
        material.setCostPerUnit(cost);
        
        material.setUnit(doc.getString("unit"));
        material.setStockQuantity(doc.getInteger("stockQuantity"));
        return material;
    }
    
    private Document materialToDocument(Material material) {
        Document doc = new Document();
        if (material.getId() != null) {
            doc.append("_id", material.getId());
        }
        doc.append("name", material.getName())
           .append("type", material.getType())
           .append("costPerUnit", material.getCostPerUnit())
           .append("unit", material.getUnit())
           .append("stockQuantity", material.getStockQuantity());
        return doc;
    }
} 