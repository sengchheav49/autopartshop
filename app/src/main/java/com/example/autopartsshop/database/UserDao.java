package com.example.autopartsshop.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.autopartsshop.models.User;

public class UserDao {
    private DatabaseHelper dbHelper;

    public UserDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // Create a new user
    public long createUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_USERNAME, user.getUsername());
        values.put(DatabaseHelper.KEY_EMAIL, user.getEmail());
        values.put(DatabaseHelper.KEY_PASSWORD, user.getPassword());
        values.put(DatabaseHelper.KEY_PHONE, user.getPhone());
        values.put(DatabaseHelper.KEY_ADDRESS, user.getAddress());
        values.put(DatabaseHelper.KEY_IS_ADMIN, user.isAdmin() ? 1 : 0);

        // ✅ Storing profile image as a byte array (BLOB)
        if (user.getProfileImage() != null) {
            values.put(DatabaseHelper.KEY_PROFILE_IMAGE, user.getProfileImage());
        }

        // Insert row
        long id = db.insert(DatabaseHelper.TABLE_USERS, null, values);
        db.close();

        return id;
    }

    // Login validation
    public User checkLogin(String email, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Detailed logging for debugging
        Log.d("LOGIN_DEBUG", "Attempting login with email: " + email + ", password: " + password);

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                new String[]{
                        DatabaseHelper.KEY_ID,
                        DatabaseHelper.KEY_USERNAME,
                        DatabaseHelper.KEY_EMAIL,
                        DatabaseHelper.KEY_PASSWORD,
                        DatabaseHelper.KEY_PHONE,
                        DatabaseHelper.KEY_ADDRESS,
                        DatabaseHelper.KEY_IS_ADMIN,
                        DatabaseHelper.KEY_CREATED_AT
                },
                DatabaseHelper.KEY_EMAIL + "=? AND " + DatabaseHelper.KEY_PASSWORD + "=?",
                new String[]{email, password},
                null, null, null, null);

        User user = null;

        if (cursor != null) {
            Log.d("LOGIN_DEBUG", "Cursor count: " + cursor.getCount());

            if (cursor.moveToFirst()) {
                int adminColumnIndex = cursor.getColumnIndex(DatabaseHelper.KEY_IS_ADMIN);
                int adminStatus = adminColumnIndex != -1 ? cursor.getInt(adminColumnIndex) : 0;

                Log.d("LOGIN_DEBUG", "Admin status: " + adminStatus);

                user = new User(
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_USERNAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_EMAIL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_PASSWORD)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_PHONE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ADDRESS)),
                        adminStatus > 0,
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_CREATED_AT))
                );

                // Additional logging
                Log.d("LOGIN_DEBUG", "User found - Username: " + user.getUsername() +
                        ", Email: " + user.getEmail() +
                        ", Is Admin: " + user.isAdmin());
            } else {
                Log.d("LOGIN_DEBUG", "No user found with given credentials");
            }
            cursor.close();
        }

        db.close();
        return user;
    }

    // Get user by ID
    public User getUserById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                new String[]{
                        DatabaseHelper.KEY_ID,
                        DatabaseHelper.KEY_USERNAME,
                        DatabaseHelper.KEY_EMAIL,
                        DatabaseHelper.KEY_PASSWORD,
                        DatabaseHelper.KEY_PHONE,
                        DatabaseHelper.KEY_ADDRESS,
                        DatabaseHelper.KEY_IS_ADMIN,
                        DatabaseHelper.KEY_CREATED_AT
                },
                DatabaseHelper.KEY_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null, null);

        User user = null;

        if (cursor != null && cursor.moveToFirst()) {
            user = new User(
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_USERNAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_EMAIL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_PASSWORD)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_PHONE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ADDRESS)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_IS_ADMIN)) == 1,
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_CREATED_AT))
            );
        }

        if (cursor != null)
            cursor.close();
        db.close();

        return user;
    }


    public User getUserByEmail(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                new String[]{
                        DatabaseHelper.KEY_ID,
                        DatabaseHelper.KEY_USERNAME,
                        DatabaseHelper.KEY_EMAIL,
                        DatabaseHelper.KEY_PASSWORD,
                        DatabaseHelper.KEY_PHONE,
                        DatabaseHelper.KEY_ADDRESS,
                        DatabaseHelper.KEY_IS_ADMIN,
                        DatabaseHelper.KEY_CREATED_AT,
                        DatabaseHelper.KEY_PROFILE_IMAGE  // Include profile image
                },
                DatabaseHelper.KEY_EMAIL + "=?",
                new String[]{email},
                null, null, null, null);

        User user = null;

        if (cursor != null && cursor.moveToFirst()) {
            user = new User(
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_USERNAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_EMAIL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_PASSWORD)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_PHONE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ADDRESS)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_IS_ADMIN)) == 1,
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_CREATED_AT))
            );

            // Fetch profile image as byte array
            byte[] imageBytes = cursor.getBlob(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_PROFILE_IMAGE));
            user.setProfileImage(imageBytes);
            Log.d("ProfileFragment", "Profile Image Bytesssssssss: " + (imageBytes != null ? "Exists" : "NULL"));
        }

        if (cursor != null)
            cursor.close();
        db.close();

        return user;
    }

    // Check if email already exists
    public boolean isEmailExists(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                new String[]{DatabaseHelper.KEY_ID},
                DatabaseHelper.KEY_EMAIL + "=?",
                new String[]{email},
                null, null, null, null);

        boolean exists = (cursor != null && cursor.getCount() > 0);

        if (cursor != null)
            cursor.close();
        db.close();

        return exists;
    }

    public int updateUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_USERNAME, user.getUsername());
        values.put(DatabaseHelper.KEY_EMAIL, user.getEmail());

        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            values.put(DatabaseHelper.KEY_PASSWORD, user.getPassword());
        }

        values.put(DatabaseHelper.KEY_PHONE, user.getPhone());
        values.put(DatabaseHelper.KEY_ADDRESS, user.getAddress());

        if (user.getProfileImage() != null) {
            values.put(DatabaseHelper.KEY_PROFILE_IMAGE, user.getProfileImage());

        }
        int rowsAffected = db.update(
                DatabaseHelper.TABLE_USERS,
                values,
                DatabaseHelper.KEY_ID + " = ?",
                new String[]{String.valueOf(user.getId())}
        );

        db.close();

        Log.d("UserDao", "Rows affected: " + rowsAffected);

        return rowsAffected;
    }

    // Print all users for debugging
    public void printAllUsers() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, null, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int idIndex = cursor.getColumnIndex(DatabaseHelper.KEY_ID);
                int usernameIndex = cursor.getColumnIndex(DatabaseHelper.KEY_USERNAME);
                int emailIndex = cursor.getColumnIndex(DatabaseHelper.KEY_EMAIL);
                int isAdminIndex = cursor.getColumnIndex(DatabaseHelper.KEY_IS_ADMIN);

                int id = cursor.getInt(idIndex);
                String username = cursor.getString(usernameIndex);
                String email = cursor.getString(emailIndex);
                int isAdmin = cursor.getInt(isAdminIndex);

                Log.d("DATABASE_USERS", "ID: " + id +
                        ", Username: " + username +
                        ", Email: " + email +
                        ", Is Admin: " + isAdmin);
            } while (cursor.moveToNext());

            cursor.close();
        }
        db.close();
    }
}