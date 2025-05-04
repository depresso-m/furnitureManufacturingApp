package com.furniture.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    private ObjectId id;
    private String name;
    private String email;
    private String phone;
    private String address;
    
    public Customer(String name, String email, String phone, String address) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }
    
    @Override
    public String toString() {
        return name + " (" + email + ")";
    }
} 