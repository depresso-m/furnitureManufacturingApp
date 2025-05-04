package com.furniture.repository;

import com.furniture.db.MongoDBService;
import com.furniture.model.Customer;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class CustomerRepository {
    private static final String COLLECTION_NAME = "customers";
    private final MongoDBService mongoDBService;
    
    public CustomerRepository() {
        this.mongoDBService = MongoDBService.getInstance();
    }
    
    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        List<Document> documents = mongoDBService.getAllDocuments(COLLECTION_NAME);
        
        for (Document doc : documents) {
            customers.add(documentToCustomer(doc));
        }
        
        return customers;
    }
    
    public Customer getCustomerById(ObjectId id) {
        Document doc = mongoDBService.getDocumentById(COLLECTION_NAME, id);
        return doc != null ? documentToCustomer(doc) : null;
    }
    
    public List<Customer> findCustomersByName(String name) {
        Pattern pattern = Pattern.compile(name, Pattern.CASE_INSENSITIVE);
        Bson filter = Filters.regex("name", pattern);
        return findCustomers(filter);
    }
    
    public List<Customer> findCustomersByEmail(String email) {
        Pattern pattern = Pattern.compile(email, Pattern.CASE_INSENSITIVE);
        Bson filter = Filters.regex("email", pattern);
        return findCustomers(filter);
    }
    
    public List<Customer> getSortedCustomers(String sortField, boolean ascending) {
        List<Customer> customers = new ArrayList<>();
        List<Document> documents = mongoDBService.getSortedDocuments(COLLECTION_NAME, sortField, ascending);
        
        for (Document doc : documents) {
            customers.add(documentToCustomer(doc));
        }
        
        return customers;
    }
    
    private List<Customer> findCustomers(Bson filter) {
        List<Customer> customers = new ArrayList<>();
        List<Document> documents = mongoDBService.findDocuments(COLLECTION_NAME, filter);
        
        for (Document doc : documents) {
            customers.add(documentToCustomer(doc));
        }
        
        return customers;
    }
    
    public boolean addCustomer(Customer customer) {
        Document doc = customerToDocument(customer);
        return mongoDBService.insertDocument(COLLECTION_NAME, doc);
    }
    
    public boolean deleteCustomer(ObjectId id) {
        return mongoDBService.deleteDocument(COLLECTION_NAME, id);
    }
    
    private Customer documentToCustomer(Document doc) {
        Customer customer = new Customer();
        customer.setId(doc.getObjectId("_id"));
        customer.setName(doc.getString("name"));
        customer.setEmail(doc.getString("email"));
        customer.setPhone(doc.getString("phone"));
        customer.setAddress(doc.getString("address"));
        return customer;
    }
    
    private Document customerToDocument(Customer customer) {
        Document doc = new Document();
        if (customer.getId() != null) {
            doc.append("_id", customer.getId());
        }
        doc.append("name", customer.getName())
           .append("email", customer.getEmail())
           .append("phone", customer.getPhone())
           .append("address", customer.getAddress());
        return doc;
    }
} 