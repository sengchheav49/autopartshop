package com.example.autopartsshop.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.autopartsshop.models.CartItem;
import com.example.autopartsshop.models.Order;
import com.example.autopartsshop.models.Product;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class OrderDao {
    private DatabaseHelper dbHelper;
    private ProductDao productDao;
    private CartDao cartDao;

    public OrderDao(Context context) {
        dbHelper = new DatabaseHelper(context);
        productDao = new ProductDao(context);
        cartDao = new CartDao(context);
    }

    // Create a new order from cart items
    public long createOrder(int userId, List<CartItem> cartItems) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Generate a random order number
        String orderNumber = "ORD-" + generateOrderNumber();

        // Calculate total amount
        double totalAmount = 0;
        for (CartItem cartItem : cartItems) {
            totalAmount += cartItem.getSubtotal();
        }

        // Create order
        ContentValues orderValues = new ContentValues();
        orderValues.put(DatabaseHelper.KEY_USER_ID, userId);
        orderValues.put(DatabaseHelper.KEY_ORDER_NUMBER, orderNumber);
        orderValues.put(DatabaseHelper.KEY_TOTAL_AMOUNT, totalAmount);
        orderValues.put(DatabaseHelper.KEY_STATUS, "Pending");

        long orderId = db.insert(DatabaseHelper.TABLE_ORDERS, null, orderValues);

        // Create order items
        if (orderId != -1) {
            for (CartItem cartItem : cartItems) {
                ContentValues itemValues = new ContentValues();
                itemValues.put(DatabaseHelper.KEY_ORDER_ID, orderId);
                itemValues.put(DatabaseHelper.KEY_PRODUCT_ID, cartItem.getProductId());
                itemValues.put(DatabaseHelper.KEY_QUANTITY, cartItem.getQuantity());
                itemValues.put(DatabaseHelper.KEY_PRODUCT_PRICE, cartItem.getProduct().getPrice());

                db.insert(DatabaseHelper.TABLE_ORDER_ITEMS, null, itemValues);

                // Update product stock
                Product product = cartItem.getProduct();
                int newStock = product.getStock() - cartItem.getQuantity();
                productDao.updateStock(product.getId(), newStock);
            }

            // Clear user's cart after successful order
            cartDao.clearCart(userId);
        }

        db.close();

        return orderId;
    }



    public Order getOrderById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_ORDERS,
                new String[]{
                        DatabaseHelper.KEY_ID,
                        DatabaseHelper.KEY_USER_ID,
                        DatabaseHelper.KEY_ORDER_NUMBER,
                        DatabaseHelper.KEY_TOTAL_AMOUNT,
                        DatabaseHelper.KEY_STATUS,
                        DatabaseHelper.KEY_CREATED_AT
                },
                DatabaseHelper.KEY_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null, null);

        Order order = null;
        if (cursor != null && cursor.moveToFirst()) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date createdAt = null;
            try {
                createdAt = dateFormat.parse(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_CREATED_AT)));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            order = new Order(
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ID)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_USER_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ORDER_NUMBER)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_TOTAL_AMOUNT)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_STATUS)),
                    createdAt
            );

            // Get order items
            order.setOrderItems(getOrderItems(order.getId()));
        }

        if (cursor != null) cursor.close();
        db.close();

        return order;
    }

    public List<Order> getUserOrders(int userId) {
        List<Order> orders = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_ORDERS,
                new String[]{
                        DatabaseHelper.KEY_ID,
                        DatabaseHelper.KEY_USER_ID,
                        DatabaseHelper.KEY_ORDER_NUMBER,
                        DatabaseHelper.KEY_TOTAL_AMOUNT,
                        DatabaseHelper.KEY_STATUS,
                        DatabaseHelper.KEY_CREATED_AT
                },
                DatabaseHelper.KEY_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, DatabaseHelper.KEY_CREATED_AT + " DESC", null);

        if (cursor.moveToFirst()) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

            do {
                Date createdAt = null;
                try {
                    createdAt = dateFormat.parse(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_CREATED_AT)));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Order order = new Order(
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_USER_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ORDER_NUMBER)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_TOTAL_AMOUNT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_STATUS)),
                        createdAt
                );

                orders.add(order);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return orders;
    }

    // Get order items
    private List<Order.OrderItem> getOrderItems(int orderId) {
        List<Order.OrderItem> orderItems = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_ORDER_ITEMS,
                new String[]{
                        DatabaseHelper.KEY_ID,
                        DatabaseHelper.KEY_ORDER_ID,
                        DatabaseHelper.KEY_PRODUCT_ID,
                        DatabaseHelper.KEY_QUANTITY,
                        DatabaseHelper.KEY_PRODUCT_PRICE
                },
                DatabaseHelper.KEY_ORDER_ID + "=?",
                new String[]{String.valueOf(orderId)},
                null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Order.OrderItem orderItem = new Order.OrderItem(
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ORDER_ID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_PRODUCT_ID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_QUANTITY)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_PRODUCT_PRICE))
                );

                // Get the associated product
                Product product = productDao.getProductById(orderItem.getProductId());
                orderItem.setProduct(product);

                orderItems.add(orderItem);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return orderItems;
    }

    // Get all orders for a user


    // Update order status
    public int updateOrderStatus(int orderId, String status) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_STATUS, status);

        // Update row
        int result = db.update(
                DatabaseHelper.TABLE_ORDERS,
                values,
                DatabaseHelper.KEY_ID + " = ?",
                new String[]{String.valueOf(orderId)}
        );

        db.close();

        return result;
    }

    // Generate a random order number
    private String generateOrderNumber() {
        // Get current date in yyyyMMdd format
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        String date = sdf.format(new Date());

        // Get a random value
        String random = UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        return date + random;
    }
}