package com.furniture.ui;

import com.furniture.ui.panels.*;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private JTabbedPane tabbedPane;
    
    public MainFrame() {
        setTitle("Система управления мебельным производством");
        initComponents();
    }
    
    private void initComponents() {
        // Create the main tabbed pane
        tabbedPane = new JTabbedPane();
        
        // Create panels for each entity
        CustomersPanel customersPanel = new CustomersPanel();
        OrdersPanel ordersPanel = new OrdersPanel();
        FurniturePanel furniturePanel = new FurniturePanel();
        MaterialsPanel materialsPanel = new MaterialsPanel();
        FurnitureDetailsPanel furnitureDetailsPanel = new FurnitureDetailsPanel();
        
        // Add panels to tabbed pane
        tabbedPane.addTab("Клиенты", new ImageIcon(), customersPanel, "Управление клиентами");
        tabbedPane.addTab("Заказы", new ImageIcon(), ordersPanel, "Управление заказами");
        tabbedPane.addTab("Мебель", new ImageIcon(), furniturePanel, "Управление каталогом мебели");
        tabbedPane.addTab("Материалы", new ImageIcon(), materialsPanel, "Управление материалами");
        tabbedPane.addTab("Детали мебели", new ImageIcon(), furnitureDetailsPanel, "Управление деталями мебели");
        
        // Initialize all panels
        customersPanel.initialize();
        ordersPanel.initialize();
        furniturePanel.initialize();
        materialsPanel.initialize();
        furnitureDetailsPanel.initialize();
        
        // Add the tabbed pane to the frame
        getContentPane().add(tabbedPane, BorderLayout.CENTER);
        
        // Create status bar
        JPanel statusBar = createStatusBar();
        getContentPane().add(statusBar, BorderLayout.SOUTH);
    }
    
    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel();
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        statusBar.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        JLabel statusLabel = new JLabel("Подключено к MongoDB");
        statusBar.add(statusLabel);
        
        return statusBar;
    }
} 