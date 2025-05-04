package com.furniture.ui.panels;

import com.furniture.model.Customer;
import com.furniture.model.Furniture;
import com.furniture.model.Order;
import com.furniture.model.Order.OrderItem;
import com.furniture.repository.CustomerRepository;
import com.furniture.repository.FurnitureRepository;
import com.furniture.repository.OrderRepository;
import org.bson.types.ObjectId;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OrdersPanel extends EntityPanel {
    private static final String[] COLUMN_NAMES = {"ID", "Клиент", "Дата", "Статус", "Общая стоимость"};
    private static final String[] SEARCH_FIELDS = {"Клиент", "Статус"};
    private static final String[] SORT_FIELDS = {"Дата", "Статус", "Общая стоимость"};
    private static final String[] ORDER_STATUS = {"Ожидается", "В процессе", "Завершен", "Отменен"};
    
    private OrderRepository orderRepository;
    private CustomerRepository customerRepository;
    private FurnitureRepository furnitureRepository;
    
    public OrdersPanel() {
        super();
        orderRepository = new OrderRepository();
        customerRepository = new CustomerRepository();
        furnitureRepository = new FurnitureRepository();
    }
    
    @Override
    protected String[] getColumnNames() {
        return COLUMN_NAMES;
    }
    
    @Override
    protected String[] getSearchFieldNames() {
        return SEARCH_FIELDS;
    }
    
    @Override
    protected String[] getSortFieldNames() {
        return SORT_FIELDS;
    }
    
    @Override
    protected void refreshData() {
        clearTable();
        List<Order> orders = orderRepository.getAllOrders();
        
        for (Order order : orders) {
            addOrderToTable(order);
        }
    }
    
    @Override
    protected void searchData() {
        String searchText = searchField.getText().trim();
        if (searchText.isEmpty()) {
            refreshData();
            return;
        }
        
        clearTable();
        List<Order> orders = new ArrayList<>();
        
        String searchType = (String) searchTypeComboBox.getSelectedItem();
        if ("Статус".equals(searchType)) {
            orders = orderRepository.getOrdersByStatus(searchText);
        } else if ("Клиент".equals(searchType)) {
            try {
                // Try first by ID
                ObjectId customerId = new ObjectId(searchText);
                orders = orderRepository.getOrdersByCustomerId(customerId);
            } catch (Exception e) {
                // Then try by name
                List<Customer> customers = customerRepository.findCustomersByName(searchText);
                for (Customer customer : customers) {
                    orders.addAll(orderRepository.getOrdersByCustomerId(customer.getId()));
                }
            }
        }
        
        for (Order order : orders) {
            addOrderToTable(order);
        }
    }
    
    @Override
    protected void showAddDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Добавить заказ", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(600, 500);
        
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Get all customers for the dropdown
        List<Customer> customers = customerRepository.getAllCustomers();
        JComboBox<Customer> customerComboBox = new JComboBox<>();
        for (Customer customer : customers) {
            customerComboBox.addItem(customer);
        }
        
        JComboBox<String> statusComboBox = new JComboBox<>(ORDER_STATUS);
        JTextField totalPriceField = new JTextField("0.0");
        totalPriceField.setEditable(false);
        
        formPanel.add(new JLabel("Клиент:"));
        formPanel.add(customerComboBox);
        formPanel.add(new JLabel("Статус:"));
        formPanel.add(statusComboBox);
        formPanel.add(new JLabel("Общая стоимость:"));
        formPanel.add(totalPriceField);
        
        // Order items table
        JPanel itemsPanel = new JPanel(new BorderLayout());
        itemsPanel.setBorder(BorderFactory.createTitledBorder("Элементы заказа"));
        
        String[] itemColumns = {"Мебель", "Количество", "Цена", "Всего"};
        DefaultTableModel itemsTableModel = new DefaultTableModel(itemColumns, 0);
        JTable itemsTable = new JTable(itemsTableModel);
        
        JScrollPane itemsScrollPane = new JScrollPane(itemsTable);
        itemsPanel.add(itemsScrollPane, BorderLayout.CENTER);
        
        // Controls for adding items
        JPanel itemControlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        // Get all furniture for the dropdown
        List<Furniture> furnitureList = furnitureRepository.getAllFurniture();
        JComboBox<Furniture> furnitureComboBox = new JComboBox<>();
        for (Furniture furniture : furnitureList) {
            furnitureComboBox.addItem(furniture);
        }
        
        JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        JButton addItemButton = new JButton("Добавить элемент");
        JButton removeItemButton = new JButton("Удалить элемент");
        
        itemControlsPanel.add(new JLabel("Мебель:"));
        itemControlsPanel.add(furnitureComboBox);
        itemControlsPanel.add(new JLabel("Количество:"));
        itemControlsPanel.add(quantitySpinner);
        itemControlsPanel.add(addItemButton);
        itemControlsPanel.add(removeItemButton);
        
        itemsPanel.add(itemControlsPanel, BorderLayout.SOUTH);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Сохранить");
        JButton cancelButton = new JButton("Отмена");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        dialog.add(formPanel, BorderLayout.NORTH);
        dialog.add(itemsPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        // Event listeners
        List<OrderItem> orderItems = new ArrayList<>();
        
        addItemButton.addActionListener(e -> {
            Furniture selectedFurniture = (Furniture) furnitureComboBox.getSelectedItem();
            if (selectedFurniture == null) {
                JOptionPane.showMessageDialog(dialog, "Пожалуйста, выберите мебель!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int quantity = (int) quantitySpinner.getValue();
            double price = selectedFurniture.getPrice();
            double total = price * quantity;
            
            // Add to the model
            Object[] rowData = {
                selectedFurniture.toString(),
                quantity,
                price,
                total
            };
            itemsTableModel.addRow(rowData);
            
            // Add to the order items list
            OrderItem item = new OrderItem(selectedFurniture.getId(), quantity, price);
            orderItems.add(item);
            
            // Update total price
            updateTotalPrice(totalPriceField, orderItems);
        });
        
        removeItemButton.addActionListener(e -> {
            int selectedRow = itemsTable.getSelectedRow();
            if (selectedRow >= 0) {
                itemsTableModel.removeRow(selectedRow);
                orderItems.remove(selectedRow);
                updateTotalPrice(totalPriceField, orderItems);
            }
        });
        
        saveButton.addActionListener(e -> {
            Customer selectedCustomer = (Customer) customerComboBox.getSelectedItem();
            if (selectedCustomer == null) {
                JOptionPane.showMessageDialog(dialog, "Пожалуйста, выберите клиента!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (orderItems.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Пожалуйста, добавьте хотя бы один элемент!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String status = (String) statusComboBox.getSelectedItem();
            double totalPrice = Double.parseDouble(totalPriceField.getText());
            
            Order order = new Order(selectedCustomer.getId(), new Date(), status, totalPrice, orderItems);
            boolean success = orderRepository.addOrder(order);
            
            if (success) {
                dialog.dispose();
                refreshData();
            } else {
                JOptionPane.showMessageDialog(dialog, "Не удалось добавить заказ!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private void updateTotalPrice(JTextField totalPriceField, List<OrderItem> items) {
        double total = 0;
        for (OrderItem item : items) {
            total += item.getPrice() * item.getQuantity();
        }
        totalPriceField.setText(String.valueOf(total));
    }
    
    @Override
    protected void deleteSelectedEntity() {
        int selectedRow = getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Пожалуйста, выберите заказ для удаления.", "Нет выбора", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String idString = (String) tableModel.getValueAt(selectedRow, 0);
        ObjectId id = new ObjectId(idString);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
                "Вы уверены, что хотите удалить этот заказ?", 
                "Подтверждение удаления", 
                JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = orderRepository.deleteOrder(id);
            
            if (success) {
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this, "Не удалось удалить заказ!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    @Override
    protected void sortData() {
        String sortField = (String) sortFieldComboBox.getSelectedItem();
        boolean ascending = "По возрастанию".equals(sortOrderComboBox.getSelectedItem());
        
        clearTable();
        
        String fieldName;
        if ("Дата".equals(sortField)) {
            fieldName = "orderDate";
        } else if ("Статус".equals(sortField)) {
            fieldName = "status";
        } else { // Общая стоимость
            fieldName = "totalPrice";
        }
        
        List<Order> orders = orderRepository.getSortedOrders(fieldName, ascending);
        
        for (Order order : orders) {
            addOrderToTable(order);
        }
    }
    
    private void addOrderToTable(Order order) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = order.getOrderDate() != null ? dateFormat.format(order.getOrderDate()) : "";
        
        // Get customer name
        String customerName = "Unknown";
        Customer customer = customerRepository.getCustomerById(order.getCustomerId());
        if (customer != null) {
            customerName = customer.getName();
        }
        
        Object[] rowData = {
            order.getId().toString(),
            customerName,
            dateStr,
            order.getStatus(),
            String.format("$%.2f", order.getTotalPrice())
        };
        tableModel.addRow(rowData);
    }
} 