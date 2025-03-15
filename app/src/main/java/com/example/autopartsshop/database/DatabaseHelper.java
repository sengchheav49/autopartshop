package com.example.autopartsshop.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    // Database Version
    public static final int DATABASE_VERSION = 4;  // Increment the version number to trigger upgrade

    // Database Name
    public static final String DATABASE_NAME = "autoparts_db";

    public static final String KEY_PROFILE_PICTURE = "profile_picture";
    public static final String TABLE_USERS = "users";
    public static final String TABLE_PRODUCTS = "products";
    public static final String TABLE_CART = "cart";
    public static final String TABLE_ORDERS = "orders";
    public static final String TABLE_ORDER_ITEMS = "order_items";
    public static final String TABLE_ADMINS = "admins";
    public static final String TABLE_BRANDS = "brands"; // New table for car brands

    // Common column names
    public static final String KEY_ID = "id";
    public static final String KEY_CREATED_AT = "created_at";
    // USERS Table - column names
    public static final String KEY_USERNAME = "username";

    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_PHONE = "phone";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_IS_ADMIN = "is_admin";

    // PRODUCTS Table - column names
    public static final String KEY_NAME = "name";
    public static final String KEY_BRAND_NAME = "brandName";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_PRICE = "price";
    public static final String KEY_CATEGORY = "category";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_LOGO_IMAGE = "logoImage";  // New column for logo image
    public static final String KEY_STOCK = "stock";
    public static final String KEY_YEAR = "year";
    public static final String KEY_MILEAGE = "mileage";
    
    // New BLOB columns for products table
    public static final String KEY_TECHNICAL_SPECS = "technical_specs";
    public static final String KEY_VIDEO_THUMBNAIL = "video_thumbnail";
    public static final String KEY_ADDITIONAL_IMAGES = "additional_images";
    public static final String KEY_MANUAL = "manual";
    
    // BRANDS Table - column names
    public static final String KEY_LOGO = "logo";
    public static final String KEY_COUNTRY_ORIGIN = "country_origin";
    public static final String KEY_WEBSITE = "website";
    public static final String KEY_IS_ACTIVE = "is_active";
    public static final String KEY_BRAND_ID = "brand_id";

    // CART Table - column names
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_PRODUCT_ID = "product_id";
    public static final String KEY_QUANTITY = "quantity";

    // ORDERS Table - column names
    public static final String KEY_ORDER_NUMBER = "order_number";
    public static final String KEY_TOTAL_AMOUNT = "total_amount";
    public static final String KEY_STATUS = "status";

    // ORDER_ITEMS Table - column names
    public static final String KEY_ORDER_ID = "order_id";
    public static final String KEY_PRODUCT_PRICE = "product_price";

    // ADMINS Table - column names
    public static final String KEY_ROLE = "role";
    public static final String KEY_BIO = "bio";
    public static final String KEY_PROFILE_IMAGE = "profile_image";
    public static final String KEY_PERMISSIONS = "permissions";

    // Constructor
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create USERS table
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_USERNAME + " TEXT,"
                + KEY_EMAIL + " TEXT UNIQUE,"
                + KEY_PASSWORD + " TEXT,"  // Hash this before storing!
                + KEY_PROFILE_IMAGE + " BLOB,"  // Fix missing space before BLOB
                + KEY_PHONE + " TEXT,"
                + KEY_ADDRESS + " TEXT,"
                + KEY_IS_ADMIN + " INTEGER DEFAULT 0,"
                + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP" + ")";
        db.execSQL(CREATE_USERS_TABLE);

        // Create BRANDS table
        String CREATE_BRANDS_TABLE = "CREATE TABLE " + TABLE_BRANDS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_NAME + " TEXT NOT NULL,"
                + KEY_LOGO + " BLOB,"
                + KEY_DESCRIPTION + " TEXT,"
                + KEY_COUNTRY_ORIGIN + " TEXT,"
                + KEY_WEBSITE + " TEXT,"
                + KEY_IS_ACTIVE + " INTEGER DEFAULT 1,"
                + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")";
        db.execSQL(CREATE_BRANDS_TABLE);

        // Create PRODUCTS table with new BLOB columns
        String CREATE_PRODUCTS_TABLE = "CREATE TABLE " + DatabaseHelper.TABLE_PRODUCTS + " (" +
                DatabaseHelper.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DatabaseHelper.KEY_NAME + " TEXT, " +
                DatabaseHelper.KEY_DESCRIPTION + " TEXT, " +
                DatabaseHelper.KEY_PRICE + " REAL, " +
                DatabaseHelper.KEY_CATEGORY + " TEXT, " +
                DatabaseHelper.KEY_STOCK + " INTEGER, " +
                DatabaseHelper.KEY_IMAGE + " BLOB, " +
                DatabaseHelper.KEY_LOGO_IMAGE + " BLOB, " +
                DatabaseHelper.KEY_BRAND_NAME + " TEXT, " +
                DatabaseHelper.KEY_BRAND_ID + " INTEGER, " +
                DatabaseHelper.KEY_YEAR + " INTEGER, " +
                DatabaseHelper.KEY_MILEAGE + " INTEGER, " +
                DatabaseHelper.KEY_TECHNICAL_SPECS + " BLOB, " +
                DatabaseHelper.KEY_VIDEO_THUMBNAIL + " BLOB, " +
                DatabaseHelper.KEY_ADDITIONAL_IMAGES + " BLOB, " +
                DatabaseHelper.KEY_MANUAL + " BLOB, " +
                DatabaseHelper.KEY_CREATED_AT + " TEXT, " +
                "FOREIGN KEY(" + KEY_BRAND_ID + ") REFERENCES " + TABLE_BRANDS + "(" + KEY_ID + "))";
        db.execSQL(CREATE_PRODUCTS_TABLE);


        // Create CART table
        String CREATE_CART_TABLE = "CREATE TABLE " + TABLE_CART + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_USER_ID + " INTEGER,"
                + KEY_PRODUCT_ID + " INTEGER,"
                + KEY_QUANTITY + " INTEGER,"
                + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY(" + KEY_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + KEY_ID + "),"
                + "FOREIGN KEY(" + KEY_PRODUCT_ID + ") REFERENCES " + TABLE_PRODUCTS + "(" + KEY_ID + ")" + ")";
        db.execSQL(CREATE_CART_TABLE);

        // Create ORDERS table
        String CREATE_ORDERS_TABLE = "CREATE TABLE " + TABLE_ORDERS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_USER_ID + " INTEGER,"
                + KEY_ORDER_NUMBER + " TEXT,"
                + KEY_TOTAL_AMOUNT + " REAL,"
                + KEY_STATUS + " TEXT,"
                + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY(" + KEY_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + KEY_ID + ")" + ")";
        db.execSQL(CREATE_ORDERS_TABLE);

        // Create ORDER_ITEMS table
        String CREATE_ORDER_ITEMS_TABLE = "CREATE TABLE " + TABLE_ORDER_ITEMS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_ORDER_ID + " INTEGER,"
                + KEY_PRODUCT_ID + " INTEGER,"
                + KEY_QUANTITY + " INTEGER,"
                + KEY_PRODUCT_PRICE + " REAL,"
                + "FOREIGN KEY(" + KEY_ORDER_ID + ") REFERENCES " + TABLE_ORDERS + "(" + KEY_ID + "),"
                + "FOREIGN KEY(" + KEY_PRODUCT_ID + ") REFERENCES " + TABLE_PRODUCTS + "(" + KEY_ID + ")" + ")";
        db.execSQL(CREATE_ORDER_ITEMS_TABLE);

        // Create ADMINS table
        String CREATE_ADMINS_TABLE = "CREATE TABLE " + TABLE_ADMINS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_NAME + " TEXT,"
                + KEY_EMAIL + " TEXT UNIQUE,"
                + KEY_PASSWORD + " TEXT,"
                + KEY_PHONE + " TEXT,"
                + KEY_ROLE + " TEXT,"
                + KEY_BIO + " TEXT,"
                + KEY_PROFILE_IMAGE + " TEXT,"
                + KEY_PERMISSIONS + " TEXT,"
                + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")";
        db.execSQL(CREATE_ADMINS_TABLE);

        // Remove previous admin insert
        String DELETE_EXISTING_ADMIN = "DELETE FROM " + TABLE_USERS + " WHERE " + KEY_IS_ADMIN + " = 1";
        db.execSQL(DELETE_EXISTING_ADMIN);

        // Insert admin user
        String INSERT_ADMIN = "INSERT INTO " + TABLE_USERS + " ("
                + KEY_USERNAME + ", " + KEY_EMAIL + ", " + KEY_PASSWORD + ", " + KEY_IS_ADMIN + ") VALUES "
                + "('sengchheav', 'sengchheav@gmail.com', 'admin123', 1)";
        db.execSQL(INSERT_ADMIN);

        // Insert admin in the ADMINS table too
        String INSERT_ADMIN_RECORD = "INSERT INTO " + TABLE_ADMINS + " ("
                + KEY_NAME + ", " + KEY_EMAIL + ", " + KEY_PASSWORD + ", " + KEY_ROLE + ", " + KEY_PERMISSIONS + ") VALUES "
                + "('sengchheav', 'sengchheav@gmail.com', 'admin123', 'Super Admin', '{}')";
        db.execSQL(INSERT_ADMIN_RECORD);

        // Insert sample car brands
        insertSampleBrands(db);
    }

    private void insertSampleBrands(SQLiteDatabase db) {
        // Insert sample car brands
        String[] brandNames = {"Toyota", "Honda", "Ford", "Chevrolet", "BMW", "Mercedes-Benz", "Audi", "Nissan"};
        for (String brandName : brandNames) {
            ContentValues values = new ContentValues();
            values.put(KEY_NAME, brandName);
            values.put(KEY_DESCRIPTION, brandName + " is a popular car manufacturer");
            db.insert(TABLE_BRANDS, null, values);
        }
    }

    // Method to print all users
    public void printAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int idIndex = cursor.getColumnIndex(KEY_ID);
                int usernameIndex = cursor.getColumnIndex(KEY_USERNAME);
                int emailIndex = cursor.getColumnIndex(KEY_EMAIL);
                int isAdminIndex = cursor.getColumnIndex(KEY_IS_ADMIN);

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

    // Method to update admin user
    public void updateAdminUser(String username, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            // Update in USERS table where is_admin is 1
            ContentValues userValues = new ContentValues();
            userValues.put(KEY_USERNAME, username);
            userValues.put(KEY_EMAIL, email);
            userValues.put(KEY_PASSWORD, password);

            db.update(TABLE_USERS, userValues, KEY_IS_ADMIN + " = ? AND " + KEY_EMAIL + " = ?",
                    new String[]{"1", email});

            // Try to update in ADMINS table too
            ContentValues adminValues = new ContentValues();
            adminValues.put(KEY_NAME, username);
            adminValues.put(KEY_EMAIL, email);
            adminValues.put(KEY_PASSWORD, password);

            db.update(TABLE_ADMINS, adminValues, KEY_EMAIL + " = ?", new String[]{email});
        } finally {
            db.close();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database schema upgrades
        if (oldVersion < 3) {
            // Add new columns to the PRODUCTS table for the additional BLOB data
            try {
                db.execSQL("ALTER TABLE " + TABLE_PRODUCTS + " ADD COLUMN " + KEY_YEAR + " INTEGER;");
                db.execSQL("ALTER TABLE " + TABLE_PRODUCTS + " ADD COLUMN " + KEY_MILEAGE + " INTEGER;");
                db.execSQL("ALTER TABLE " + TABLE_PRODUCTS + " ADD COLUMN " + KEY_TECHNICAL_SPECS + " BLOB;");
                db.execSQL("ALTER TABLE " + TABLE_PRODUCTS + " ADD COLUMN " + KEY_VIDEO_THUMBNAIL + " BLOB;");
                db.execSQL("ALTER TABLE " + TABLE_PRODUCTS + " ADD COLUMN " + KEY_ADDITIONAL_IMAGES + " BLOB;");
                db.execSQL("ALTER TABLE " + TABLE_PRODUCTS + " ADD COLUMN " + KEY_MANUAL + " BLOB;");
                Log.d("DATABASE_UPGRADE", "Added new BLOB columns to products table");
            } catch (Exception e) {
                Log.e("DATABASE_UPGRADE", "Error adding columns: " + e.getMessage());
                // If adding columns fails, recreate the tables
                recreateAllTables(db);
            }
        }
        
        if (oldVersion < 4) {
            try {
                // Create the BRANDS table if it doesn't exist
                String CREATE_BRANDS_TABLE = "CREATE TABLE " + TABLE_BRANDS + "("
                        + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + KEY_NAME + " TEXT NOT NULL,"
                        + KEY_LOGO + " BLOB,"
                        + KEY_DESCRIPTION + " TEXT,"
                        + KEY_COUNTRY_ORIGIN + " TEXT,"
                        + KEY_WEBSITE + " TEXT,"
                        + KEY_IS_ACTIVE + " INTEGER DEFAULT 1,"
                        + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                        + ")";
                db.execSQL(CREATE_BRANDS_TABLE);
                
                // Add brand_id column to PRODUCTS table if it doesn't exist
                db.execSQL("ALTER TABLE " + TABLE_PRODUCTS + " ADD COLUMN " + KEY_BRAND_ID + " INTEGER;");
                
                // Insert sample brands
                insertSampleBrands(db);
                
                Log.d("DATABASE_UPGRADE", "Added brands table and brand_id column to products table");
            } catch (Exception e) {
                Log.e("DATABASE_UPGRADE", "Error upgrading to version 4: " + e.getMessage());
            }
        }
    }
    
    // Helper method to recreate all tables in case upgrade fails
    private void recreateAllTables(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDER_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CART);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BRANDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ADMINS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);

        // Create tables again
        onCreate(db);
    }
}