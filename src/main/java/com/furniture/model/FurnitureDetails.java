package com.furniture.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FurnitureDetails {
    private ObjectId id;
    private ObjectId furnitureId;
    private String description;
    private String dimensions; // e.g., "100x50x75 cm"
    private double weight;
    private List<MaterialUsage> materialUsages;
    
    public FurnitureDetails(ObjectId furnitureId, String description, String dimensions, 
                           double weight, List<MaterialUsage> materialUsages) {
        this.furnitureId = furnitureId;
        this.description = description;
        this.dimensions = dimensions;
        this.weight = weight;
        this.materialUsages = materialUsages;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MaterialUsage {
        private ObjectId materialId;
        private double quantity;
    }
    
    @Override
    public String toString() {
        return dimensions + ", " + weight + "kg, " + (materialUsages != null ? materialUsages.size() : 0) + " materials";
    }
} 