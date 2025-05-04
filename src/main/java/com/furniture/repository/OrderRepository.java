package com.furniture.repository;

import com.furniture.db.MongoDBService;
import com.furniture.model.Order;
import com.furniture.model.Order.OrderItem;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OrderRepository {
    private static final String COLLECTION_NAME = "orders";
    private final MongoDBService mongoDBService;
    
    public OrderRepository() {
        this.mongoDBService = MongoDBService.getInstance();
    }
    
    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        List<Document> documents = mongoDBService.getAllDocuments(COLLECTION_NAME);
        
        for (Document doc : documents) {
            orders.add(documentToOrder(doc));
        }
        
        return orders;
    }
    
    public Order getOrderById(ObjectId id) {
        Document doc = mongoDBService.getDocumentById(COLLECTION_NAME, id);
        return doc != null ? documentToOrder(doc) : null;
    }
    
    public List<Order> getOrdersByCustomerId(ObjectId customerId) {
        Bson filter = Filters.eq("customerId", customerId);
        return findOrders(filter);
    }
    
    public List<Order> getOrdersByStatus(String status) {
        Bson filter = Filters.eq("status", status);
        return findOrders(filter);
    }
    
    public List<Order> getSortedOrders(String sortField, boolean ascending) {
        List<Order> orders = new ArrayList<>();
        List<Document> documents = mongoDBService.getSortedDocuments(COLLECTION_NAME, sortField, ascending);
        
        for (Document doc : documents) {
            orders.add(documentToOrder(doc));
        }
        
        return orders;
    }
    
    private List<Order> findOrders(Bson filter) {
        List<Order> orders = new ArrayList<>();
        List<Document> documents = mongoDBService.findDocuments(COLLECTION_NAME, filter);
        
        for (Document doc : documents) {
            orders.add(documentToOrder(doc));
        }
        
        return orders;
    }
    
    public boolean addOrder(Order order) {
        Document doc = orderToDocument(order);
        return mongoDBService.insertDocument(COLLECTION_NAME, doc);
    }
    
    public boolean deleteOrder(ObjectId id) {
        return mongoDBService.deleteDocument(COLLECTION_NAME, id);
    }
    
    private Order documentToOrder(Document doc) {
        Order order = new Order();
        order.setId(doc.getObjectId("_id"));
        order.setCustomerId(doc.getObjectId("customerId"));
        order.setOrderDate(doc.getDate("orderDate"));
        order.setStatus(doc.getString("status"));
        
        Object priceObj = doc.get("totalPrice");
        double totalPrice = 0.0;
        if (priceObj instanceof Integer) {
            totalPrice = ((Integer) priceObj).doubleValue();
        } else if (priceObj instanceof Double) {
            totalPrice = (Double) priceObj;
        }
        order.setTotalPrice(totalPrice);
        
        List<OrderItem> items = new ArrayList<>();
        List<Document> itemDocs = (List<Document>) doc.get("items");
        if (itemDocs != null) {
            for (Document itemDoc : itemDocs) {
                ObjectId furnitureId = itemDoc.getObjectId("furnitureId");
                int quantity = itemDoc.getInteger("quantity");
                
                Object itemPriceObj = itemDoc.get("price");
                double itemPrice = 0.0;
                if (itemPriceObj instanceof Integer) {
                    itemPrice = ((Integer) itemPriceObj).doubleValue();
                } else if (itemPriceObj instanceof Double) {
                    itemPrice = (Double) itemPriceObj;
                }
                
                OrderItem item = new OrderItem(furnitureId, quantity, itemPrice);
                items.add(item);
            }
        }
        order.setItems(items);
        
        return order;
    }
    
    private Document orderToDocument(Order order) {
        Document doc = new Document();
        if (order.getId() != null) {
            doc.append("_id", order.getId());
        }
        doc.append("customerId", order.getCustomerId())
           .append("orderDate", order.getOrderDate() != null ? order.getOrderDate() : new Date())
           .append("status", order.getStatus())
           .append("totalPrice", order.getTotalPrice());
        
        List<Document> itemDocs = new ArrayList<>();
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                Document itemDoc = new Document()
                    .append("furnitureId", item.getFurnitureId())
                    .append("quantity", item.getQuantity())
                    .append("price", item.getPrice());
                itemDocs.add(itemDoc);
            }
        }
        doc.append("items", itemDocs);
        
        return doc;
    }
} 