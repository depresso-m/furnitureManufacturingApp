package com.furniture;

import com.furniture.db.MongoDBService;
import com.furniture.ui.MainFrame;

import javax.swing.*;
import java.awt.*;

public class FurnitureManufacturingApp {
    public static void main(String[] args) {
        try {
            // Set system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Initialize the MongoDB service
        MongoDBService.getInstance();
        
        // Start the application on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainFrame.setSize(1000, 700);
            mainFrame.setLocationRelativeTo(null);
            mainFrame.setVisible(true);
            
            // Handle application shutdown
            mainFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                    MongoDBService.getInstance().close();
                }
            });
        });
    }
} 