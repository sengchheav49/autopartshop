package com.example.autopartsshop.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.autopartsshop.models.CartItem;
import com.example.autopartsshop.models.Product;

import java.util.ArrayList;
import java.util.List;

public class CartDao {
    private DatabaseHelper dbHelper;
    private ProductDao productDao;

    public CartDao(Context context) {
        dbHelper = new DatabaseHelper(context);
        productDao = new ProductDao(context);
    }

    // Add item to cart
    public long addToCart(CartItem cartItem) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Check if product already exists in cart
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_CART,
                new String[]{DatabaseHelper.KEY_ID, DatabaseHelper.KEY_QUANTITY},
                DatabaseHelper.KEY_USER_ID + "=? AND " + DatabaseHelper.KEY_PRODUCT_ID + "=?",
                new String[]{String.valueOf(cartItem.getUserId()), String.valueOf(cartItem.getProductId())},
                null, null, null, null);

        long id;

        if (cursor != null && cursor.moveToFirst()) {
            // Product already exists in cart, update quantity
            int existingQuantity = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_QUANTITY));
            int newQuantity = existingQuantity + cartItem.getQuantity();

            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.KEY_QUANTITY, newQuantity);

            id = db.update(
                    DatabaseHelper.TABLE_CART,
                    values,
                    DatabaseHelper.KEY_USER_ID + "=? AND " + DatabaseHelper.KEY_PRODUCT_ID + "=?",
                    new String[]{String.valueOf(cartItem.getUserId()), String.valueOf(cartItem.getProductId())}
            );
        } else {
            // Product not in cart, add it
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.KEY_USER_ID, cartItem.getUserId());
            values.put(DatabaseHelper.KEY_PRODUCT_ID, cartItem.getProductId());
            values.put(DatabaseHelper.KEY_QUANTITY, cartItem.getQuantity());

            id = db.insert(DatabaseHelper.TABLE_CART, null, values);
        }

        if (cursor != null)
            cursor.close();
        db.close();

        return id;
    }

    // Get cart items for a user
    public List<CartItem> getCartItems(int userId) {
        List<CartItem> cartItems = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT c.*, p." + DatabaseHelper.KEY_NAME + ", p." + DatabaseHelper.KEY_PRICE +
                ", p." + DatabaseHelper.KEY_IMAGE + ", p." + DatabaseHelper.KEY_STOCK +
                " FROM " + DatabaseHelper.TABLE_CART + " c" +
                " INNER JOIN " + DatabaseHelper.TABLE_PRODUCTS + " p" +
                " ON c." + DatabaseHelper.KEY_PRODUCT_ID + " = p." + DatabaseHelper.KEY_ID +
                " WHERE c." + DatabaseHelper.KEY_USER_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                CartItem cartItem = new CartItem(
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_USER_ID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_PRODUCT_ID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_QUANTITY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_CREATED_AT))
                );

                // Get the associated product
                Product product = productDao.getProductById(cartItem.getProductId());
                cartItem.setProduct(product);

                cartItems.add(cartItem);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return cartItems;
    }

    // Update cart item quantity
    public int updateCartItemQuantity(int cartItemId, int quantity) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_QUANTITY, quantity);

        // Update row
        int result = db.update(
                DatabaseHelper.TABLE_CART,
                values,
                DatabaseHelper.KEY_ID + " = ?",
                new String[]{String.valueOf(cartItemId)}
        );

        db.close();

        return result;
    }

    // Remove item from cart
    public void removeFromCart(int cartItemId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.delete(
                DatabaseHelper.TABLE_CART,
                DatabaseHelper.KEY_ID + " = ?",
                new String[]{String.valueOf(cartItemId)}
        );

        db.close();
    }

    // Clear cart for a user
    public void clearCart(int userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.delete(
                DatabaseHelper.TABLE_CART,
                DatabaseHelper.KEY_USER_ID + " = ?",
                new String[]{String.valueOf(userId)}
        );

        db.close();
    }

    // Get cart item count
    public int getCartItemCount(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String countQuery = "SELECT SUM(" + DatabaseHelper.KEY_QUANTITY + ") as total FROM " +
                DatabaseHelper.TABLE_CART +
                " WHERE " + DatabaseHelper.KEY_USER_ID + " = " + userId;

        Cursor cursor = db.rawQuery(countQuery, null);

        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }

        db.close();

        return count;
    }

    // Calculate cart total
    public double getCartTotal(int userId) {
        double total = 0;

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT c." + DatabaseHelper.KEY_QUANTITY + ", p." + DatabaseHelper.KEY_PRICE +
                " FROM " + DatabaseHelper.TABLE_CART + " c" +
                " INNER JOIN " + DatabaseHelper.TABLE_PRODUCTS + " p" +
                " ON c." + DatabaseHelper.KEY_PRODUCT_ID + " = p." + DatabaseHelper.KEY_ID +
                " WHERE c." + DatabaseHelper.KEY_USER_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_QUANTITY));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_PRICE));

                total += quantity * price;
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return total;
    }
}