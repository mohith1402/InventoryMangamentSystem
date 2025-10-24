package model;

// A simple class to represent a single item in the inventory.
public class InventoryItem {
    private String name;
    private int quantity;
    private double price;

    public InventoryItem(String name, int quantity, double price) {
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }

    // --- Getters and Setters (Essential for OOP encapsulation) ---
    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    // Override toString() for easy display in the JTextArea
    @Override
    public String toString() {
        return String.format("%-20s | Qty: %-5d | Price: ₹%.2f", name, quantity, price);
    }
}