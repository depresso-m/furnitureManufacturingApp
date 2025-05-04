package com.furniture.ui.panels;

import com.furniture.model.Material;
import com.furniture.repository.MaterialRepository;
import org.bson.types.ObjectId;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MaterialsPanel extends EntityPanel {
    private static final String[] COLUMN_NAMES = {"ID", "Название", "Тип", "Цена за единицу", "Единица измерения", "Наличие"};
    private static final String[] SEARCH_FIELDS = {"Название", "Тип"};
    private static final String[] SORT_FIELDS = {"Название", "Тип", "Цена", "Наличие"};
    private static final String[] MATERIAL_TYPES = {
        "Дерево", "Металл", "Ткань", "Пластик", "Стекло", "Кожа", "Другое"
    };
    private static final String[] MATERIAL_UNITS = {
        "м", "м²", "кг", "шт", "литр"
    };
    
    private MaterialRepository materialRepository;
    
    public MaterialsPanel() {
        super();
        materialRepository = new MaterialRepository();
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
        List<Material> materials = materialRepository.getAllMaterials();
        
        for (Material material : materials) {
            addMaterialToTable(material);
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
        List<Material> materials;
        
        String searchType = (String) searchTypeComboBox.getSelectedItem();
        if ("Название".equals(searchType)) {
            materials = materialRepository.findMaterialsByName(searchText);
        } else { // Тип
            materials = materialRepository.findMaterialsByType(searchText);
        }
        
        for (Material material : materials) {
            addMaterialToTable(material);
        }
    }
    
    @Override
    protected void showAddDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Добавить материал", true);
        dialog.setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTextField nameField = new JTextField(20);
        JComboBox<String> typeComboBox = new JComboBox<>(MATERIAL_TYPES);
        JTextField costField = new JTextField(20);
        JComboBox<String> unitComboBox = new JComboBox<>(MATERIAL_UNITS);
        JSpinner stockSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 10000, 1));
        
        formPanel.add(new JLabel("Название:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Тип:"));
        formPanel.add(typeComboBox);
        formPanel.add(new JLabel("Цена за единицу:"));
        formPanel.add(costField);
        formPanel.add(new JLabel("Единица измерения:"));
        formPanel.add(unitComboBox);
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
            String type = (String) typeComboBox.getSelectedItem();
            String costText = costField.getText().trim();
            String unit = (String) unitComboBox.getSelectedItem();
            int stockQuantity = (int) stockSpinner.getValue();
            
            if (name.isEmpty() || costText.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Название и цена за единицу обязательны!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            double costPerUnit;
            try {
                costPerUnit = Double.parseDouble(costText);
                if (costPerUnit <= 0) {
                    throw new NumberFormatException("Цена должна быть положительной");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Неверный формат цены!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Material material = new Material(name, type, costPerUnit, unit, stockQuantity);
            boolean success = materialRepository.addMaterial(material);
            
            if (success) {
                dialog.dispose();
                refreshData();
            } else {
                JOptionPane.showMessageDialog(dialog, "Не удалось добавить материал!", "Ошибка", JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(this, "Пожалуйста, выберите материал для удаления.", "Нет выбора", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String idString = (String) tableModel.getValueAt(selectedRow, 0);
        ObjectId id = new ObjectId(idString);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
                "Вы уверены, что хотите удалить этот материал?", 
                "Подтверждение удаления", 
                JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = materialRepository.deleteMaterial(id);
            
            if (success) {
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this, "Не удалось удалить материал!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    @Override
    protected void sortData() {
        String sortField = (String) sortFieldComboBox.getSelectedItem();
        boolean ascending = "Ascending".equals(sortOrderComboBox.getSelectedItem());
        
        clearTable();
        
        String fieldName;
        if ("Название".equals(sortField)) {
            fieldName = "name";
        } else if ("Тип".equals(sortField)) {
            fieldName = "type";
        } else if ("Цена".equals(sortField)) {
            fieldName = "costPerUnit";
        } else { // Наличие
            fieldName = "stockQuantity";
        }
        
        List<Material> materials = materialRepository.getSortedMaterials(fieldName, ascending);
        
        for (Material material : materials) {
            addMaterialToTable(material);
        }
    }
    
    private void addMaterialToTable(Material material) {
        Object[] rowData = {
            material.getId().toString(),
            material.getName(),
            material.getType(),
            String.format("$%.2f", material.getCostPerUnit()),
            material.getUnit(),
            material.getStockQuantity()
        };
        tableModel.addRow(rowData);
    }
} 