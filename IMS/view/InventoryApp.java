package view;

import service.InventoryManager;
import model.InventoryItem;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.SQLException;
import java.util.List;

public class InventoryApp extends JFrame {

    private InventoryManager manager = new InventoryManager(); 
    
    // UI Components
    private JTextField nameField, quantityField, priceField;
    private JTextField searchField; 
    private JButton addButton, deleteButton, sellButton, searchButton; 
    private JTextArea displayArea;

    // --- CONSTRUCTOR ---
    public InventoryApp() {
        // --- Frame Setup ---
        setTitle("Inventory Management System");
        setSize(800, 600); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // --- 1. Top Input Panel (NORTH) ---
        JPanel itemInputPanel = new JPanel(new GridLayout(3, 2, 8, 8));
        itemInputPanel.setBorder(BorderFactory.createTitledBorder("Item Details (Add/Update/Sell)"));
        
        nameField = new JTextField(20);
        // 🚨 NEW: Add KeyListener to the item name field for scanning
        nameField.addKeyListener(new BarcodeKeyListener());
        // Set focus initially to make it ready for a scan
        nameField.requestFocusInWindow(); 

        quantityField = new JTextField(10);
        priceField = new JTextField(10);

        itemInputPanel.add(new JLabel("Item Name:")); // Updated label for clarity
        itemInputPanel.add(nameField);
        itemInputPanel.add(new JLabel("Quantity (New/Sell):"));
        itemInputPanel.add(quantityField);
        itemInputPanel.add(new JLabel("Price (₹):")); 
        itemInputPanel.add(priceField);
        
        // --- 2. Control/Action Panel (CENTER-TOP) ---
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        
        addButton = new JButton("Add/Update Stock");
        sellButton = new JButton("Sell Stock");
        deleteButton = new JButton("Delete Item");
        
        addButton.addActionListener(this::addItemAction);
        sellButton.addActionListener(this::sellItemAction);
        deleteButton.addActionListener(this::deleteItemAction);
        
        // --- 3. Search Bar ---
        searchField = new JTextField(15);
        searchButton = new JButton("Search");
        searchButton.addActionListener(this::searchInventory);
        
        controlPanel.add(addButton);
        controlPanel.add(sellButton);
        controlPanel.add(deleteButton);

        JPanel searchBarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        searchBarPanel.add(new JLabel("Search Item:"));
        searchBarPanel.add(searchField);
        searchBarPanel.add(searchButton);

        // Group all NORTH components
        JPanel northContainer = new JPanel(new BorderLayout());
        northContainer.add(itemInputPanel, BorderLayout.NORTH);
        northContainer.add(controlPanel, BorderLayout.CENTER);
        northContainer.add(searchBarPanel, BorderLayout.SOUTH);
        
        add(northContainer, BorderLayout.NORTH);

        // --- 4. Display Area (CENTER) ---
        displayArea = new JTextArea();
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        displayArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(displayArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Current Stock Listing (Click Refresh to view all)"));

        add(scrollPane, BorderLayout.CENTER);
        
        // Add a view button to the bottom for manual refresh
        JButton refreshButton = new JButton("View All Inventory / Refresh");
        refreshButton.addActionListener(_ -> viewInventory()); 
        add(refreshButton, BorderLayout.SOUTH);

        viewInventory(); // Load initial data
        setLocationRelativeTo(null); 
        setVisible(true);
    } 

    // ------------------------------------------------------------------
    // 👇 NEW FEATURE: KeyListener for Barcode Scanning
    // ------------------------------------------------------------------

    private class BarcodeKeyListener implements KeyListener {
        @Override
        public void keyTyped(KeyEvent e) {
            // Not used for this logic
        }

        @Override
        public void keyPressed(KeyEvent e) {
            // Check if the Enter key (VK_ENTER) was pressed. 
            // This is the signal that the scanner finished typing the barcode.
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                // Prevent the default Swing 'Enter' action (e.g., button click)
                e.consume(); 
                
                // Use invokeLater to ensure the text field is fully updated
                SwingUtilities.invokeLater(() -> {
                    processBarcodeScan();
                });
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            // Not used for this logic
        }
    }

    private void processBarcodeScan() {
        String barcode = nameField.getText().trim();
        
        if (barcode.isEmpty()) {
            return;
        }

        try {
            // 1. Check if the item exists in the inventory
            List<InventoryItem> results = manager.searchItems(barcode);
            
            if (results.isEmpty()) {
                // Item not found, assume user wants to ADD it.
                JOptionPane.showMessageDialog(this, 
                    "Barcode '" + barcode + "' not found. Ready to ADD new item.", 
                    "New Item Scan", JOptionPane.INFORMATION_MESSAGE);
                
                // Clear all fields except the name/barcode field
                quantityField.setText("");
                priceField.setText("");
                quantityField.requestFocus(); // Move focus to Quantity for adding details
                
            } else if (results.size() == 1) {
                // Item found, assume user wants to SELL it (or update quantity).
                InventoryItem item = results.get(0);

                // Set relevant fields
                nameField.setText(item.getName()); 
                priceField.setText(String.format("%.2f", item.getPrice()));
                quantityField.setText(""); // Ready for quantity to sell/add
                
                // Display the single item/search result
                displayInventory(results, "--- Item Found by Scan ---");
                
                // Move focus to Quantity for selling/adding
                quantityField.requestFocus();

            } else {
                // Should not happen if barcodes are unique, but handle multiple matches
                JOptionPane.showMessageDialog(this, 
                    "Multiple items found matching '" + barcode + "'. Please refine search.", 
                    "Ambiguous Scan", JOptionPane.WARNING_MESSAGE);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error during barcode lookup: " + ex.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    // ------------------------------------------------------------------
    // 👇 STANDARD HELPER AND ACTION METHODS (Modified in previous steps)
    // ------------------------------------------------------------------

    private void displayInventory(List<InventoryItem> items, String title) {
        displayArea.setText(""); 
        displayArea.append(title + "\n");
        
        displayArea.append(String.format("%-20s | %-5s | %-12s | %s\n", 
                                          "ITEM NAME", "QTY", "UNIT PRICE (₹)", "TOTAL VALUE (₹)"));
        displayArea.append("----------------------------------------------------------------\n");
        
        if (items.isEmpty()) {
            displayArea.append("The inventory is currently empty or no items match the criteria.");
        } else {
            for (InventoryItem item : items) {
                String name = item.getName();
                int quantity = item.getQuantity();
                double unitPrice = item.getPrice(); 
                double totalPrice = quantity * unitPrice; 

                String outputLine = String.format("%-20s | %-5d | ₹%-11.2f | ₹%.2f\n", 
                                                  name, 
                                                  quantity, 
                                                  unitPrice, 
                                                  totalPrice);
                displayArea.append(outputLine);
            }
        }
    }
    
    private void viewInventory() {
        try {
            List<InventoryItem> currentItems = manager.getAllItems();
            displayInventory(currentItems, "--- FULL STOCK LISTING ---");
        } catch (SQLException ex) {
            displayArea.setText("ERROR: Could not load inventory from database. Check your JDBC setup.");
            System.err.println("SQL Error: " + ex.getMessage());
        }
    }

    private void clearFields() {
        nameField.setText("");
        quantityField.setText("");
        priceField.setText("");
        nameField.requestFocus();
    }
    
    // --- Action Methods ---

    private void addItemAction(ActionEvent e) {
        String name = nameField.getText().trim();
        int quantity = 0;
        double price = 0.0;
        
        try {
            quantity = Integer.parseInt(quantityField.getText().trim());
            price = Double.parseDouble(priceField.getText().trim());

            if (name.isEmpty() || quantity <= 0 || price <= 0) {
                JOptionPane.showMessageDialog(this, "Please enter valid item details (Name, Quantity > 0, Price > 0).", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            manager.saveItem(name, quantity, price); 
            
            clearFields();
            viewInventory(); 
            JOptionPane.showMessageDialog(this, "Item processed successfully! (Stock Updated/Added)", "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Quantity and Price must be valid numbers.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) { 
            JOptionPane.showMessageDialog(this, "Database Error during Add/Update: " + ex.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void sellItemAction(ActionEvent e) {
        String name = nameField.getText().trim();
        int quantityToSell = 0; 

        try {
            quantityToSell = Integer.parseInt(quantityField.getText().trim());

            if (name.isEmpty() || quantityToSell <= 0) {
                JOptionPane.showMessageDialog(this, "Enter item name and quantity > 0 to sell.", "Input Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            double currentPrice = manager.getItemPrice(name); 
            
            if (currentPrice == 0.0) {
                 JOptionPane.showMessageDialog(this, "Item '" + name + "' not found or price is 0.0. Please add item first.", "Item Not Found", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            priceField.setText(String.format("%.2f", currentPrice));

            String result = manager.sellItem(name, quantityToSell, currentPrice); 
            
            clearFields();
            viewInventory(); 
            
            int messageType = result.startsWith("Error") ? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE;
            JOptionPane.showMessageDialog(this, result, "Sales Status", messageType);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Quantity must be a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error during Sale lookup: " + ex.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteItemAction(ActionEvent e) {
        String name = nameField.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter the item name to delete in the 'Item Name' field.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String result = manager.deleteItem(name);
            clearFields();
            viewInventory(); 
            JOptionPane.showMessageDialog(this, result, "Deletion Status", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error during Deletion: " + ex.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void searchInventory(ActionEvent e) {
        String query = searchField.getText().trim();
        
        if (query.isEmpty()) {
            viewInventory(); 
            return;
        }
        
        try {
            List<InventoryItem> results = manager.searchItems(query);
            displayInventory(results, "--- Search Results for '" + query + "' ---");
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Search Database Error: " + ex.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new InventoryApp());
    }
}