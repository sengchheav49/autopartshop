package com.example.autopartsshop.models;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Order {
    private int id;
    private int userId;
    private String orderNumber;
    private double totalAmount;
    private String status;
    private Date createdAt;

    // List to store order items
    private List<OrderItem> orderItems;

    // Constructor with id

    public Order(int id, int userId, String orderNumber, double totalAmount, String status, Date createdAt) {
        this.id = id;
        this.userId = userId;
        this.orderNumber = orderNumber;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
        this.orderItems = new ArrayList<>();
    }

    // Constructor without id (sets current date)
    public Order(int userId, String orderNumber, double totalAmount, String status) {
        this.userId = userId;
        this.orderNumber = orderNumber;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = new Date(); // Set current date and time
        this.orderItems = new ArrayList<>();
    }
    public String getFormattedCreatedAt() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()); // Remove time part
        return sdf.format(createdAt);
    }

    // Empty constructor
    public Order() {
        this.orderItems = new ArrayList<>();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    // Add an item to the order
    public void addOrderItem(OrderItem item) {
        this.orderItems.add(item);
    }

    // Format total amount as string with currency
    public String getFormattedTotalAmount() {
        return String.format("$%.2f", totalAmount);
    }

    // Inner class to represent an order item
    public static class OrderItem {
        private int id;
        private int orderId;
        private int productId;
        private int quantity;
        private double productPrice;

        // Additional field to store product details
        private Product product;

        // Constructor with id
        public OrderItem(int id, int orderId, int productId, int quantity, double productPrice) {
            this.id = id;
            this.orderId = orderId;
            this.productId = productId;
            this.quantity = quantity;
            this.productPrice = productPrice;
        }

        // Constructor without id for new order item creation
        public OrderItem(int orderId, int productId, int quantity, double productPrice) {
            this.orderId = orderId;
            this.productId = productId;
            this.quantity = quantity;
            this.productPrice = productPrice;
        }

        // Constructor from cart item
        public OrderItem(int orderId, CartItem cartItem) {
            this.orderId = orderId;
            this.productId = cartItem.getProductId();
            this.quantity = cartItem.getQuantity();
            this.productPrice = cartItem.getProduct().getPrice();
            this.product = cartItem.getProduct();
        }

        // Empty constructor
        public OrderItem() {
        }

        // Getters and Setters
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getOrderId() {
            return orderId;
        }

        public void setOrderId(int orderId) {
            this.orderId = orderId;
        }

        public int getProductId() {
            return productId;
        }

        public void setProductId(int productId) {
            this.productId = productId;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public double getProductPrice() {
            return productPrice;
        }

        public void setProductPrice(double productPrice) {
            this.productPrice = productPrice;
        }

        public Product getProduct() {
            return product;
        }

        public void setProduct(Product product) {
            this.product = product;
        }

        // Calculate subtotal for this order item
        public double getSubtotal() {
            return productPrice * quantity;
        }

        // Format subtotal as string with currency
        public String getFormattedSubtotal() {
            return String.format("$%.2f", getSubtotal());
        }

        // Format product price as string with currency
        public String getFormattedProductPrice() {
            return String.format("$%.2f", productPrice);
        }
    }
}