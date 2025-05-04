package com.furniture.ui.panels;

import com.furniture.model.Furniture;
import com.furniture.model.FurnitureDetails;
import com.furniture.model.FurnitureDetails.MaterialUsage;
import com.furniture.model.Material;
import com.furniture.repository.FurnitureDetailsRepository;
import com.furniture.repository.FurnitureRepository;
import com.furniture.repository.MaterialRepository;
import org.bson.types.ObjectId;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FurnitureDetailsPanel extends EntityPanel {
    private static final String[] COLUMN_NAMES = {"ID", "Мебель", "Размеры", "Вес", "Используемые материалы"};
    private static final String[] SEARCH_FIELDS = {"Мебель"};
    private static final String[] SORT_FIELDS = {"Вес"};
    
    private FurnitureDetailsRepository furnitureDetailsRepository;
    private FurnitureRepository furnitureRepository;
    private MaterialRepository materialRepository;
    
    public FurnitureDetailsPanel() {
        super();
        furnitureDetailsRepository = new FurnitureDetailsRepository();
        furnitureRepository = new FurnitureRepository();
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
        List<FurnitureDetails> detailsList = furnitureDetailsRepository.getAllFurnitureDetails();
        
        for (FurnitureDetails details : detailsList) {
            addFurnitureDetailsToTable(details);
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
        
        try {
            // Try to find by furniture ID
            ObjectId furnitureId = new ObjectId(searchText);
            FurnitureDetails details = furnitureDetailsRepository.getFurnitureDetailsByFurnitureId(furnitureId);
            if (details != null) {
                addFurnitureDetailsToTable(details);
            }
        } catch (Exception e) {
            // Try to find by furniture name
            List<Furniture> furnitureList = furnitureRepository.findFurnitureByName(searchText);
            for (Furniture furniture : furnitureList) {
                FurnitureDetails details = furnitureDetailsRepository.getFurnitureDetailsByFurnitureId(furniture.getId());
                if (details != null) {
                    addFurnitureDetailsToTable(details);
                }
            }
        }
    }
    
    @Override
    protected void showAddDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Добавить детали мебели", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(600, 500);
        
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Get all furniture for the dropdown
        List<Furniture> furnitureList = furnitureRepository.getAllFurniture();
        JComboBox<Furniture> furnitureComboBox = new JComboBox<>();
        for (Furniture furniture : furnitureList) {
            furnitureComboBox.addItem(furniture);
        }
        
        JTextField dimensionsField = new JTextField(20);
        JTextField weightField = new JTextField(20);
        JTextArea descriptionArea = new JTextArea(5, 20);
        JScrollPane descriptionScrollPane = new JScrollPane(descriptionArea);
        
        formPanel.add(new JLabel("Мебель:"));
        formPanel.add(furnitureComboBox);
        formPanel.add(new JLabel("Размеры (например, 100x50x75 см):"));
        formPanel.add(dimensionsField);
        formPanel.add(new JLabel("Вес (кг):"));
        formPanel.add(weightField);
        
        JPanel descriptionPanel = new JPanel(new BorderLayout());
        descriptionPanel.setBorder(BorderFactory.createTitledBorder("Описание"));
        descriptionPanel.add(descriptionScrollPane, BorderLayout.CENTER);
        
        // Materials panel
        JPanel materialsPanel = new JPanel(new BorderLayout());
        materialsPanel.setBorder(BorderFactory.createTitledBorder("Используемые материалы"));
        
        String[] materialColumns = {"Материал", "Количество"};
        DefaultTableModel materialsTableModel = new DefaultTableModel(materialColumns, 0);
        JTable materialsTable = new JTable(materialsTableModel);
        
        JScrollPane materialsScrollPane = new JScrollPane(materialsTable);
        materialsPanel.add(materialsScrollPane, BorderLayout.CENTER);
        
        // Controls for adding materials
        JPanel materialControlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        // Get all materials for the dropdown
        List<Material> materialsList = materialRepository.getAllMaterials();
        JComboBox<Material> materialComboBox = new JComboBox<>();
        for (Material material : materialsList) {
            materialComboBox.addItem(material);
        }
        
        JTextField quantityField = new JTextField(5);
        JButton addMaterialButton = new JButton("Добавить материал");
        JButton removeMaterialButton = new JButton("Удалить материал");
        
        materialControlsPanel.add(new JLabel("Материал:"));
        materialControlsPanel.add(materialComboBox);
        materialControlsPanel.add(new JLabel("Количество:"));
        materialControlsPanel.add(quantityField);
        materialControlsPanel.add(addMaterialButton);
        materialControlsPanel.add(removeMaterialButton);
        
        materialsPanel.add(materialControlsPanel, BorderLayout.SOUTH);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Сохранить");
        JButton cancelButton = new JButton("Отмена");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(formPanel, BorderLayout.NORTH);
        contentPanel.add(descriptionPanel, BorderLayout.CENTER);
        contentPanel.add(materialsPanel, BorderLayout.SOUTH);
        
        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        // Event listeners
        List<MaterialUsage> materialUsages = new ArrayList<>();
        
        addMaterialButton.addActionListener(e -> {
            Material selectedMaterial = (Material) materialComboBox.getSelectedItem();
            if (selectedMaterial == null) {
                JOptionPane.showMessageDialog(dialog, "Пожалуйста, выберите материал!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String quantityText = quantityField.getText().trim();
            if (quantityText.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Пожалуйста, укажите количество!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            double quantity;
            try {
                quantity = Double.parseDouble(quantityText);
                if (quantity <= 0) {
                    throw new NumberFormatException("Количество должно быть положительным");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Неверный формат количества!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Add to the model
            Object[] rowData = {
                selectedMaterial.toString(),
                quantity + " " + selectedMaterial.getUnit()
            };
            materialsTableModel.addRow(rowData);
            
            // Add to the material usages list
            MaterialUsage usage = new MaterialUsage(selectedMaterial.getId(), quantity);
            materialUsages.add(usage);
            
            // Clear the quantity field
            quantityField.setText("");
        });
        
        removeMaterialButton.addActionListener(e -> {
            int selectedRow = materialsTable.getSelectedRow();
            if (selectedRow >= 0) {
                materialsTableModel.removeRow(selectedRow);
                materialUsages.remove(selectedRow);
            }
        });
        
        saveButton.addActionListener(e -> {
            Furniture selectedFurniture = (Furniture) furnitureComboBox.getSelectedItem();
            if (selectedFurniture == null) {
                JOptionPane.showMessageDialog(dialog, "Пожалуйста, выберите элемент мебели!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String dimensions = dimensionsField.getText().trim();
            String weightText = weightField.getText().trim();
            String description = descriptionArea.getText().trim();
            
            if (dimensions.isEmpty() || weightText.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Размеры и вес обязательны!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            double weight;
            try {
                weight = Double.parseDouble(weightText);
                if (weight <= 0) {
                    throw new NumberFormatException("Вес должен быть положительным");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Неверный формат веса!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            FurnitureDetails details = new FurnitureDetails(
                selectedFurniture.getId(), 
                description, 
                dimensions, 
                weight, 
                materialUsages
            );
            
            boolean success = furnitureDetailsRepository.addFurnitureDetails(details);
            
            if (success) {
                dialog.dispose();
                refreshData();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to add furniture details!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    @Override
    protected void deleteSelectedEntity() {
        int selectedRow = getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Пожалуйста, выберите детали мебели для удаления.", "Нет выбора", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String idString = (String) tableModel.getValueAt(selectedRow, 0);
        ObjectId id = new ObjectId(idString);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
                "Вы уверены, что хотите удалить эти детали мебели?", 
                "Подтверждение удаления", 
                JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = furnitureDetailsRepository.deleteFurnitureDetails(id);
            
            if (success) {
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this, "Не удалось удалить детали мебели!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    @Override
    protected void sortData() {
        String sortField = (String) sortFieldComboBox.getSelectedItem();
        boolean ascending = "Ascending".equals(sortOrderComboBox.getSelectedItem());
        
        clearTable();
        
        // For now, we only sort by weight
        List<FurnitureDetails> detailsList = furnitureDetailsRepository.getSortedFurnitureDetails("weight", ascending);
        
        for (FurnitureDetails details : detailsList) {
            addFurnitureDetailsToTable(details);
        }
    }
    
    private void addFurnitureDetailsToTable(FurnitureDetails details) {
        // Get furniture name
        String furnitureName = "Unknown";
        Furniture furniture = furnitureRepository.getFurnitureById(details.getFurnitureId());
        if (furniture != null) {
            furnitureName = furniture.getName();
        }
        
        Object[] rowData = {
            details.getId().toString(),
            furnitureName,
            details.getDimensions(),
            details.getWeight() + " kg",
            details.getMaterialUsages() != null ? details.getMaterialUsages().size() + " materials" : "0 materials"
        };
        tableModel.addRow(rowData);
    }
} 