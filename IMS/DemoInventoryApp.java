import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

// Class to hold simple item data for the demo display
class DemoItem {
    private String name;
    private int quantity;
    private double price;

    public DemoItem(String name, int quantity, double price) {
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }

    @Override
    public String toString() {
        return String.format("%-20s | Qty: %-5d | Price: ₹%.2f", name, quantity, price);
    }
}

public class DemoInventoryApp extends JFrame {

    // UI Components
    private JTextField nameField, quantityField, priceField;
    private JTextField searchField;
    private JTextArea displayArea;

    // Simple in-memory list for demo data
    private List<DemoItem> demoInventory = new ArrayList<>();

    public DemoInventoryApp() {
        // --- Initialize Demo Data ---
        demoInventory.add(new DemoItem("Laptop Pro", 15, 85000.00));
        demoInventory.add(new DemoItem("Monitor 4K", 30, 22500.00));
        demoInventory.add(new DemoItem("Wireless Mouse", 150, 850.00));
        
        // --- Frame Setup ---
        setTitle("Inventory Management System(Demo)");
        setSize(750, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // --- 1. Main Input Panel (NORTH) ---
        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 8, 8));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Item Operations (Add/Modify)"));
        
        nameField = new JTextField(20);
        quantityField = new JTextField(10);
        priceField = new JTextField(10);

        inputPanel.add(new JLabel("Item Name:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Quantity:"));
        inputPanel.add(quantityField);
        inputPanel.add(new JLabel("Price (₹):")); 
        inputPanel.add(priceField);
        
        // --- 2. Action Buttons Panel (CENTER-TOP) ---
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        
        JButton addButton = new JButton("Add/Update Item");
        JButton sellButton = new JButton("Sell/Reduce Stock");
        JButton deleteButton = new JButton("Delete Item");
        
        // --- 3. Search Panel (CENTER-TOP - integrated with actions) ---
        searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        
        // Add basic functionality for UI demo (using dummy methods)
        addButton.addActionListener(this::demoAction);
        sellButton.addActionListener(this::demoAction);
        deleteButton.addActionListener(this::demoAction);
        searchButton.addActionListener(this::demoSearchAction);
        
        actionPanel.add(addButton);
        actionPanel.add(sellButton);
        actionPanel.add(deleteButton);
        actionPanel.add(new JLabel("Search:"));
        actionPanel.add(searchField);
        actionPanel.add(searchButton);

        // Group Input and Actions
        JPanel northContainer = new JPanel(new BorderLayout());
        northContainer.add(inputPanel, BorderLayout.NORTH);
        northContainer.add(actionPanel, BorderLayout.CENTER);
        
        add(northContainer, BorderLayout.NORTH);


        // --- 4. Display Area (CENTER) ---
        displayArea = new JTextArea();
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        displayArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(displayArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Current Stock Listing(uses JDBC in final product.)"));

        add(scrollPane, BorderLayout.CENTER);

        viewInventory(); // Load initial demo data
        
        setLocationRelativeTo(null); // Center the window
        setVisible(true);
    }
    
    // --- Demo Action Handlers ---

    // Generic handler to show button interaction is successful
    private void demoAction(ActionEvent e) {
        String action = ((JButton)e.getSource()).getText();
        JOptionPane.showMessageDialog(this, action + " button clicked! UI is responsive.", "Demo Success", JOptionPane.INFORMATION_MESSAGE);
        clearFields();
        viewInventory(); // Reload to show consistency
    }
    
    // Simple search demo (just shows a message)
    private void demoSearchAction(ActionEvent e) {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Search field is empty. Showing full list.", "Demo Info", JOptionPane.INFORMATION_MESSAGE);
            viewInventory();
            return;
        }
         JOptionPane.showMessageDialog(this, "Searching for '" + query + "'. (Demo only - Logic omitted)", "Search Demo", JOptionPane.INFORMATION_MESSAGE);
         searchField.setText("");
         viewInventory();
    }

    // Method to display the hardcoded demo list
    private void viewInventory() {
        displayArea.setText(""); 
        displayArea.append(String.format("%-20s | %-5s | %s\n", "ITEM NAME", "QTY", "PRICE (₹)"));
        displayArea.append("----------------------------------------------------\n");
        
        for (DemoItem item : demoInventory) {
            displayArea.append(item.toString() + "\n");
        }
    }
    
    private void clearFields() {
        nameField.setText("");
        quantityField.setText("");
        priceField.setText("");
        nameField.requestFocus();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DemoInventoryApp());
    }
}