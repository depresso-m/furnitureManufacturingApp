package com.furniture.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private ObjectId id;
    private ObjectId customerId;
    private Date orderDate;
    private String status; // Pending, In Progress, Completed, Cancelled
    private double totalPrice;
    private List<OrderItem> items;
    
    public Order(ObjectId customerId, Date orderDate, String status, double totalPrice, List<OrderItem> items) {
        this.customerId = customerId;
        this.orderDate = orderDate;
        this.status = status;
        this.totalPrice = totalPrice;
        this.items = items;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        private ObjectId furnitureId;
        private int quantity;
        private double price;
    }
    
    @Override
    public String toString() {
        return "Order #" + (id != null ? id.toString().substring(0, 6) : "New") + 
               " - " + status + " - $" + totalPrice;
    }
} 