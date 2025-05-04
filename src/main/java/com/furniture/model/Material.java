package com.furniture.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Material {
    private ObjectId id;
    private String name;
    private String type;
    private double costPerUnit;
    private String unit; // meters, kg, etc.
    private int stockQuantity;
    
    public Material(String name, String type, double costPerUnit, String unit, int stockQuantity) {
        this.name = name;
        this.type = type;
        this.costPerUnit = costPerUnit;
        this.unit = unit;
        this.stockQuantity = stockQuantity;
    }
    
    @Override
    public String toString() {
        return name + " (" + type + ") - " + costPerUnit + " per " + unit;
    }
} 