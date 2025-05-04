package com.furniture.ui.panels;

import com.furniture.model.Customer;
import com.furniture.repository.CustomerRepository;
import org.bson.types.ObjectId;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class CustomersPanel extends EntityPanel {
    private static final String[] COLUMN_NAMES = {"ID", "Имя", "Email", "Телефон", "Адрес"};
    private static final String[] SEARCH_FIELDS = {"Имя", "Email"};
    private static final String[] SORT_FIELDS = {"Имя", "Email"};
    
    private CustomerRepository customerRepository;
    
    public CustomersPanel() {
        super();
        customerRepository = new CustomerRepository();
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
        List<Customer> customers = customerRepository.getAllCustomers();
        
        for (Customer customer : customers) {
            addCustomerToTable(customer);
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
        List<Customer> customers;
        
        String searchType = (String) searchTypeComboBox.getSelectedItem();
        if ("Имя".equals(searchType)) {
            customers = customerRepository.findCustomersByName(searchText);
        } else { // Email
            customers = customerRepository.findCustomersByEmail(searchText);
        }
        
        for (Customer customer : customers) {
            addCustomerToTable(customer);
        }
    }
    
    @Override
    protected void showAddDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Добавить клиента", true);
        dialog.setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTextField nameField = new JTextField(20);
        JTextField emailField = new JTextField(20);
        JTextField phoneField = new JTextField(20);
        JTextField addressField = new JTextField(20);
        
        formPanel.add(new JLabel("Имя:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("Телефон:"));
        formPanel.add(phoneField);
        formPanel.add(new JLabel("Адрес:"));
        formPanel.add(addressField);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Сохранить");
        JButton cancelButton = new JButton("Отмена");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        saveButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String address = addressField.getText().trim();
            
            if (name.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Имя и Email обязательны!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Customer customer = new Customer(name, email, phone, address);
            boolean success = customerRepository.addCustomer(customer);
            
            if (success) {
                dialog.dispose();
                refreshData();
            } else {
                JOptionPane.showMessageDialog(dialog, "Не удалось добавить клиента!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    @Override
    protected void deleteSelectedEntity() {
        int selectedRow = getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Пожалуйста, выберите клиента для удаления.", "Нет выбора", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String idString = (String) tableModel.getValueAt(selectedRow, 0);
        ObjectId id = new ObjectId(idString);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
                "Вы уверены, что хотите удалить этого клиента?", 
                "Подтверждение удаления", 
                JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = customerRepository.deleteCustomer(id);
            
            if (success) {
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this, "Не удалось удалить клиента!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    @Override
    protected void sortData() {
        String sortField = (String) sortFieldComboBox.getSelectedItem();
        boolean ascending = "По возрастанию".equals(sortOrderComboBox.getSelectedItem());
        
        clearTable();
        
        String fieldName;
        if ("Имя".equals(sortField)) {
            fieldName = "name";
        } else { // Email
            fieldName = "email";
        }
        
        List<Customer> customers = customerRepository.getSortedCustomers(fieldName, ascending);
        
        for (Customer customer : customers) {
            addCustomerToTable(customer);
        }
    }
    
    private void addCustomerToTable(Customer customer) {
        Object[] rowData = {
            customer.getId().toString(),
            customer.getName(),
            customer.getEmail(),
            customer.getPhone(),
            customer.getAddress()
        };
        tableModel.addRow(rowData);
    }
} 