package com.furniture.ui.panels;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.Vector;

public abstract class EntityPanel extends JPanel {
    protected JTable table;
    protected DefaultTableModel tableModel;
    protected JTextField searchField;
    protected JComboBox<String> searchTypeComboBox;
    protected JComboBox<String> sortFieldComboBox;
    protected JButton addButton;
    protected JButton deleteButton;
    protected JButton searchButton;
    protected JButton refreshButton;
    protected JComboBox<String> sortOrderComboBox;
    protected JButton sortButton;
    
    public EntityPanel() {
        setLayout(new BorderLayout());
        initComponents();
        initEventListeners();
    }
    
    public void initialize() {
        refreshData();
    }
    
    protected void initComponents() {
        // Top panel with controls
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        searchTypeComboBox = new JComboBox<>(getSearchFieldNames());
        searchButton = new JButton("Поиск");
        refreshButton = new JButton("Обновить");
        searchPanel.add(new JLabel("Искать по:"));
        searchPanel.add(searchTypeComboBox);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(refreshButton);
        topPanel.add(searchPanel, BorderLayout.WEST);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addButton = new JButton("Добавить");
        deleteButton = new JButton("Удалить");
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        topPanel.add(buttonPanel, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Table
        tableModel = createTableModel();
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
        
        // Bottom panel with sorting controls
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        sortFieldComboBox = new JComboBox<>(getSortFieldNames());
        sortOrderComboBox = new JComboBox<>(new String[]{"По возрастанию", "По убыванию"});
        sortButton = new JButton("Сортировать");
        
        bottomPanel.add(new JLabel("Сортировать по:"));
        bottomPanel.add(sortFieldComboBox);
        bottomPanel.add(sortOrderComboBox);
        bottomPanel.add(sortButton);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    protected void initEventListeners() {
        searchButton.addActionListener(e -> searchData());
        refreshButton.addActionListener(e -> refreshData());
        addButton.addActionListener(e -> showAddDialog());
        deleteButton.addActionListener(e -> deleteSelectedEntity());
        sortButton.addActionListener(e -> sortData());
    }
    
    protected abstract String[] getColumnNames();
    
    protected abstract String[] getSearchFieldNames();
    
    protected abstract String[] getSortFieldNames();
    
    protected abstract void refreshData();
    
    protected abstract void searchData();
    
    protected abstract void showAddDialog();
    
    protected abstract void deleteSelectedEntity();
    
    protected abstract void sortData();
    
    protected DefaultTableModel createTableModel() {
        return new DefaultTableModel(getColumnNames(), 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }
    
    protected void clearTable() {
        tableModel.setRowCount(0);
    }
    
    protected int getSelectedRow() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) {
            return -1;
        }
        return table.convertRowIndexToModel(viewRow);
    }
} 