package service;

import model.InventoryItem;
import data.DatabaseConnection; // Import the connection class
import java.util.ArrayList;
import java.util.List;
import java.sql.*; // Import all JDBC classes

public class InventoryManager {

    public InventoryManager() {
        // Initialize the DB table when the manager is created
        DatabaseConnection.initializeDatabase();
    }

    // --- Method 1: Add or Update Item (REPLACING ArrayList logic) ---
    public void saveItem(String name, int quantity, double price) throws SQLException {
        // SQL to check if the item already exists
        String checkSql = "SELECT quantity FROM items WHERE name = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            
            checkStmt.setString(1, name);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next()) {
                // Item exists: UPDATE the quantity
                int existingQty = rs.getInt("quantity");
                int newQty = existingQty + quantity;
                
                String updateSql = "UPDATE items SET quantity = ?, price = ? WHERE name = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setInt(1, newQty);
                    updateStmt.setDouble(2, price);
                    updateStmt.setString(3, name);
                    updateStmt.executeUpdate();
                }
            } else {
                // Item does not exist: INSERT a new record
                String insertSql = "INSERT INTO items (name, quantity, price) VALUES (?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, name);
                    insertStmt.setInt(2, quantity);
                    insertStmt.setDouble(3, price);
                    insertStmt.executeUpdate();
                }
            }
        }
    }
    
    // --- Method 2: Get All Items (REPLACING ArrayList retrieval) ---
    public List<InventoryItem> getAllItems() throws SQLException {
        List<InventoryItem> items = new ArrayList<>();
        String sql = "SELECT name, quantity, price FROM items ORDER BY name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String name = rs.getString("name");
                int quantity = rs.getInt("quantity");
                double price = rs.getDouble("price");
                items.add(new InventoryItem(name, quantity, price));
            }
        }
        return items;
    }

    // --- Method 3: Delete Item (REPLACING ArrayList deletion) ---
    public String deleteItem(String name) throws SQLException {
        String sql = "DELETE FROM items WHERE name = ?";
        int rowsAffected;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, name);
            rowsAffected = stmt.executeUpdate();
        }
        
        if (rowsAffected > 0) {
            return "Item '" + name + "' successfully deleted.";
        } else {
            return "Item '" + name + "' not found.";
        }
    }
}