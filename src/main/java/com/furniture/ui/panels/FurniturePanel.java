package com.furniture.ui.panels;

import com.furniture.model.Furniture;
import com.furniture.repository.FurnitureRepository;
import org.bson.types.ObjectId;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class FurniturePanel extends EntityPanel {
    private static final String[] COLUMN_NAMES = {"ID", "Название", "Категория", "Цена", "Наличие"};
    private static final String[] SEARCH_FIELDS = {"Название", "Категория"};
    private static final String[] SORT_FIELDS = {"Название", "Цена", "Наличие"};
    private static final String[] FURNITURE_CATEGORIES = {
        "Стол", "Стул", "Диван", "Кровать", "Шкаф", "Книжная полка", "Письменный стол", "Шкафчик", "Другое"
    };
    
    private FurnitureRepository furnitureRepository;
    
    public FurniturePanel() {
        super();
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
        List<Furniture> furnitureList = furnitureRepository.getAllFurniture();
        
        for (Furniture furniture : furnitureList) {
            addFurnitureToTable(furniture);
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
        List<Furniture> furnitureList;
        
        String searchType = (String) searchTypeComboBox.getSelectedItem();
        if ("Название".equals(searchType)) {
            furnitureList = furnitureRepository.findFurnitureByName(searchText);
        } else { // Категория
            furnitureList = furnitureRepository.findFurnitureByCategory(searchText);
        }
        
        for (Furniture furniture : furnitureList) {
            addFurnitureToTable(furniture);
        }
    }
    
    @Override
    protected void showAddDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Добавить мебель", true);
        dialog.setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTextField nameField = new JTextField(20);
        JComboBox<String> categoryComboBox = new JComboBox<>(FURNITURE_CATEGORIES);
        JTextField priceField = new JTextField(20);
        JSpinner stockSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 1000, 1));
        
        formPanel.add(new JLabel("Название:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Категория:"));
        formPanel.add(categoryComboBox);
        formPanel.add(new JLabel("Цена:"));
        formPanel.add(priceField);
        formPanel.add(new JLabel("Количество на складе:"));
        formPanel.add(stockSpinner);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Сохранить");
        JButton cancelButton = new JButton("Отмена");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        saveButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String category = (String) categoryComboBox.getSelectedItem();
            String priceText = priceField.getText().trim();
            int stockQuantity = (int) stockSpinner.getValue();
            
            if (name.isEmpty() || priceText.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Название и цена обязательны!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            double price;
            try {
                price = Double.parseDouble(priceText);
                if (price <= 0) {
                    throw new NumberFormatException("Цена должна быть положительной");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Неверный формат цены!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Furniture furniture = new Furniture(name, category, price, stockQuantity);
            boolean success = furnitureRepository.addFurniture(furniture);
            
            if (success) {
                dialog.dispose();
                refreshData();
            } else {
                JOptionPane.showMessageDialog(dialog, "Не удалось добавить мебель!", "Ошибка", JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(this, "Пожалуйста, выберите мебель для удаления.", "Нет выбора", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String idString = (String) tableModel.getValueAt(selectedRow, 0);
        ObjectId id = new ObjectId(idString);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
                "Вы уверены, что хотите удалить эту мебель?", 
                "Подтверждение удаления", 
                JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = furnitureRepository.deleteFurniture(id);
            
            if (success) {
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this, "Не удалось удалить мебель!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    @Override
    protected void sortData() {
        String sortField = (String) sortFieldComboBox.getSelectedItem();
        boolean ascending = "По возрастанию".equals(sortOrderComboBox.getSelectedItem());
        
        clearTable();
        
        String fieldName;
        if ("Название".equals(sortField)) {
            fieldName = "name";
        } else if ("Цена".equals(sortField)) {
            fieldName = "price";
        } else { // Наличие
            fieldName = "stockQuantity";
        }
        
        List<Furniture> furnitureList = furnitureRepository.getSortedFurniture(fieldName, ascending);
        
        for (Furniture furniture : furnitureList) {
            addFurnitureToTable(furniture);
        }
    }
    
    private void addFurnitureToTable(Furniture furniture) {
        Object[] rowData = {
            furniture.getId().toString(),
            furniture.getName(),
            furniture.getCategory(),
            String.format("$%.2f", furniture.getPrice()),
            furniture.getStockQuantity()
        };
        tableModel.addRow(rowData);
    }
} 