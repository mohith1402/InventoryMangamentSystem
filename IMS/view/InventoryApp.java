package view;

import service.InventoryManager;
import model.InventoryItem;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.List;

public class InventoryApp extends JFrame {

    // Instantiate the business logic layer
    private InventoryManager manager = new InventoryManager(); 
    
    // UI Components
    private JTextField nameField, quantityField, priceField;
    private JTextArea displayArea;
    private JButton addButton, deleteButton;

    public InventoryApp() {
        // --- Frame Setup ---
        setTitle("Inventory Management System");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // --- 1. Top Input Panel (NORTH) ---
        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Add/Update Item"));
        
        nameField = new JTextField(20);
        quantityField = new JTextField(10);
        priceField = new JTextField(10);

        inputPanel.add(new JLabel("Item Name:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Quantity:"));
        inputPanel.add(quantityField);
        // Changed to INR (₹)
        inputPanel.add(new JLabel("Price (₹):")); 
        inputPanel.add(priceField);

        // --- 2. Button Control Panel (SOUTH) ---
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        
        addButton = new JButton("Add/Update Item");
        deleteButton = new JButton("Delete Item (by Name)");
        
        // Use method reference (preferred over lambda for readability)
        addButton.addActionListener(this::addItemAction);
        deleteButton.addActionListener(this::deleteItemAction);
        
        // Use lambda with ignored parameter (e.g., '_') to avoid compiler warning
        JButton viewInventoryButton = new JButton("View Inventory (Refreshes List)");
        viewInventoryButton.addActionListener(_ -> viewInventory());

        controlPanel.add(addButton);
        controlPanel.add(deleteButton);
        controlPanel.add(viewInventoryButton);

        // --- 3. Display Area (CENTER) ---
        displayArea = new JTextArea();
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        displayArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(displayArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Current Stock List"));

        // Add panels to the Frame
        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        viewInventory(); // Load initial data on startup
        setLocationRelativeTo(null); // Center the window
        setVisible(true);
    }

    // --- Action Methods (Handling user input and connecting to manager) ---

    private void addItemAction(ActionEvent e) {
        try {
            String name = nameField.getText().trim();
            int quantity = Integer.parseInt(quantityField.getText().trim());
            double price = Double.parseDouble(priceField.getText().trim());

            if (name.isEmpty() || quantity <= 0 || price <= 0) {
                JOptionPane.showMessageDialog(this, "Please enter valid data.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Calls the database saving logic
            manager.saveItem(name, quantity, price); 
            
            clearFields();
            viewInventory();
            JOptionPane.showMessageDialog(this, "Item processed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Quantity and Price must be valid numbers.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) { // <-- Handle Database Errors
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteItemAction(ActionEvent e) {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter the item name to delete in the 'Item Name' field.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String result = manager.deleteItem(name); // Calls the database deletion logic
            viewInventory();
            clearFields();
            JOptionPane.showMessageDialog(this, result, "Deletion Status", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) { // <-- Handle Database Errors
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewInventory() {
        displayArea.setText(""); // Clear area
        
        try {
            List<InventoryItem> currentItems = manager.getAllItems();
            
            if (currentItems.isEmpty()) {
                displayArea.append("The inventory is currently empty.");
            } else {
                // Display Header
                displayArea.append(String.format("%-20s | %-5s | %s\n", "ITEM NAME", "QTY", "PRICE (₹)"));
                displayArea.append("----------------------------------------------------\n");
                
                for (InventoryItem item : currentItems) {
                    displayArea.append(item.toString() + "\n");
                }
            }
        } catch (SQLException ex) {
            displayArea.append("ERROR: Could not load inventory from database. Check your JDBC setup.");
            System.err.println("SQL Error: " + ex.getMessage());
        }
    }

    private void clearFields() {
        nameField.setText("");
        quantityField.setText("");
        priceField.setText("");
        nameField.requestFocus();
    }

    public static void main(String[] args) {
        // Run the GUI on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> new InventoryApp());
    }
}