package com.example.autopartsshop.utils;

public class Constants {

    // Intent extra keys
    public static final String EXTRA_PRODUCT_ID = "product_id";
    public static final String EXTRA_CATEGORY = "category";
    public static final String EXTRA_ORDER_ID = "order_id";

    // Order status constants
    public static final String ORDER_STATUS_PENDING = "Pending";
    public static final String ORDER_STATUS_PROCESSING = "Processing";
    public static final String ORDER_STATUS_SHIPPED = "Shipped";
    public static final String ORDER_STATUS_DELIVERED = "Delivered";
    public static final String ORDER_STATUS_CANCELLED = "Cancelled";

    // Payment methods
    public static final String PAYMENT_METHOD_CASH = "Cash on Delivery";
    public static final String PAYMENT_METHOD_CREDIT_CARD = "Credit Card";
    public static final String PAYMENT_METHOD_PAYPAL = "PayPal";

    // Color resources
    public static final String COLOR_PRIMARY = "#1E88E5";       // Blue
    public static final String COLOR_PRIMARY_DARK = "#1565C0";  // Darker Blue
    public static final String COLOR_ACCENT = "#FF5722";        // Orange
    public static final String COLOR_BACKGROUND = "#FAFAFA";    // Light Grey
    public static final String COLOR_TEXT_PRIMARY = "#212121";  // Almost Black
    public static final String COLOR_TEXT_SECONDARY = "#757575"; // Grey

    // Default values
    public static final int DEFAULT_QUANTITY = 1;
    public static final int MAX_QUANTITY = 10;
}