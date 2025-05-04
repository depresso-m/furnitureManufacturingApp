package com.furniture.repository;

import com.furniture.db.MongoDBService;
import com.furniture.model.FurnitureDetails;
import com.furniture.model.FurnitureDetails.MaterialUsage;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class FurnitureDetailsRepository {
    private static final String COLLECTION_NAME = "furnitureDetails";
    private final MongoDBService mongoDBService;
    
    public FurnitureDetailsRepository() {
        this.mongoDBService = MongoDBService.getInstance();
    }
    
    public List<FurnitureDetails> getAllFurnitureDetails() {
        List<FurnitureDetails> detailsList = new ArrayList<>();
        List<Document> documents = mongoDBService.getAllDocuments(COLLECTION_NAME);
        
        for (Document doc : documents) {
            detailsList.add(documentToFurnitureDetails(doc));
        }
        
        return detailsList;
    }
    
    public FurnitureDetails getFurnitureDetailsById(ObjectId id) {
        Document doc = mongoDBService.getDocumentById(COLLECTION_NAME, id);
        return doc != null ? documentToFurnitureDetails(doc) : null;
    }
    
    public FurnitureDetails getFurnitureDetailsByFurnitureId(ObjectId furnitureId) {
        Bson filter = Filters.eq("furnitureId", furnitureId);
        List<Document> docs = mongoDBService.findDocuments(COLLECTION_NAME, filter);
        return !docs.isEmpty() ? documentToFurnitureDetails(docs.get(0)) : null;
    }
    
    public List<FurnitureDetails> getSortedFurnitureDetails(String sortField, boolean ascending) {
        List<FurnitureDetails> detailsList = new ArrayList<>();
        List<Document> documents = mongoDBService.getSortedDocuments(COLLECTION_NAME, sortField, ascending);
        
        for (Document doc : documents) {
            detailsList.add(documentToFurnitureDetails(doc));
        }
        
        return detailsList;
    }
    
    public boolean addFurnitureDetails(FurnitureDetails details) {
        Document doc = furnitureDetailsToDocument(details);
        return mongoDBService.insertDocument(COLLECTION_NAME, doc);
    }
    
    public boolean deleteFurnitureDetails(ObjectId id) {
        return mongoDBService.deleteDocument(COLLECTION_NAME, id);
    }
    
    private FurnitureDetails documentToFurnitureDetails(Document doc) {
        FurnitureDetails details = new FurnitureDetails();
        details.setId(doc.getObjectId("_id"));
        details.setFurnitureId(doc.getObjectId("furnitureId"));
        details.setDescription(doc.getString("description"));
        details.setDimensions(doc.getString("dimensions"));
        
        Object weightObj = doc.get("weight");
        double weight = 0.0;
        if (weightObj instanceof Integer) {
            weight = ((Integer) weightObj).doubleValue();
        } else if (weightObj instanceof Double) {
            weight = (Double) weightObj;
        }
        details.setWeight(weight);
        
        List<MaterialUsage> materialUsages = new ArrayList<>();
        List<Document> usageDocs = (List<Document>) doc.get("materialUsages");
        if (usageDocs != null) {
            for (Document usageDoc : usageDocs) {
                ObjectId materialId = usageDoc.getObjectId("materialId");
                
                Object quantityObj = usageDoc.get("quantity");
                double quantity = 0.0;
                if (quantityObj instanceof Integer) {
                    quantity = ((Integer) quantityObj).doubleValue();
                } else if (quantityObj instanceof Double) {
                    quantity = (Double) quantityObj;
                }
                
                MaterialUsage usage = new MaterialUsage(materialId, quantity);
                materialUsages.add(usage);
            }
        }
        details.setMaterialUsages(materialUsages);
        
        return details;
    }
    
    private Document furnitureDetailsToDocument(FurnitureDetails details) {
        Document doc = new Document();
        if (details.getId() != null) {
            doc.append("_id", details.getId());
        }
        doc.append("furnitureId", details.getFurnitureId())
           .append("description", details.getDescription())
           .append("dimensions", details.getDimensions())
           .append("weight", details.getWeight());
        
        List<Document> usageDocs = new ArrayList<>();
        if (details.getMaterialUsages() != null) {
            for (MaterialUsage usage : details.getMaterialUsages()) {
                Document usageDoc = new Document()
                    .append("materialId", usage.getMaterialId())
                    .append("quantity", usage.getQuantity());
                usageDocs.add(usageDoc);
            }
        }
        doc.append("materialUsages", usageDocs);
        
        return doc;
    }
} 