package service;

import model.InventoryItem;
import data.DatabaseConnection;
import java.util.ArrayList;
import java.util.List;
import java.sql.*;

public class InventoryManager {

    public InventoryManager() {
        DatabaseConnection.initializeDatabase();
    }

    // 1. Adds or Updates item
    public void saveItem(String name, int quantity, double price) throws SQLException {
        String checkSql = "SELECT quantity FROM items WHERE name = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            
            checkStmt.setString(1, name);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next()) {
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
    
    // 2. Fetches all items
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
    
    // 3. Deletes an item
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

    // 4. Reduces stock for a sale (Updates quantity and price)
    public String sellItem(String name, int quantityToSell, double newPrice) throws SQLException {
        String checkSql = "SELECT quantity FROM items WHERE name = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            
            checkStmt.setString(1, name);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next()) {
                int currentQty = rs.getInt("quantity");
                if (currentQty < quantityToSell) {
                    return "Error: Insufficient stock. Only " + currentQty + " units available.";
                }

                int newQty = currentQty - quantityToSell;
                
                String updateSql = "UPDATE items SET quantity = ?, price = ? WHERE name = ?"; 
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setInt(1, newQty);
                    updateStmt.setDouble(2, newPrice); 
                    updateStmt.setString(3, name);
                    updateStmt.executeUpdate();

                    // ⚠️ NEW: Calculate the total remaining value
                    double remainingValue = newQty * newPrice;

                    return "Successfully sold " + quantityToSell + " units of " + name + 
                           ". Stock remaining: " + newQty + 
                           ". Remaining Stock Total Value: ₹" + String.format("%.2f", remainingValue);
                }
            } else {
                return "Error: Item '" + name + "' not found in inventory.";
            }
        }
    }
    
    // 5. Search/Filter Items by Name
    public List<InventoryItem> searchItems(String nameQuery) throws SQLException {
        List<InventoryItem> items = new ArrayList<>();
        String sql = "SELECT name, quantity, price FROM items WHERE name LIKE ? ORDER BY name"; 
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + nameQuery + "%"); 
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("name");
                    int quantity = rs.getInt("quantity");
                    double price = rs.getDouble("price");
                    items.add(new InventoryItem(name, quantity, price));
                }
            }
        }
        return items;
    }

    // 6. Get the current price of a single item
    public double getItemPrice(String name) throws SQLException {
        String sql = "SELECT price FROM items WHERE name = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, name);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("price");
                }
            }
        }
        return 0.0; 
    }
}