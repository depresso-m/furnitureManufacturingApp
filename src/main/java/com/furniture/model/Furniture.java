package com.furniture.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Furniture {
    private ObjectId id;
    private String name;
    private String category;
    private double price;
    private int stockQuantity;
    
    public Furniture(String name, String category, double price, int stockQuantity) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }
    
    @Override
    public String toString() {
        return name + " - $" + price;
    }
} 