package com.example.autopartsshop.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.autopartsshop.models.Brand;
import com.example.autopartsshop.utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;

public class BrandDao {
    private static final String TAG = "BrandDao";
    private DatabaseHelper dbHelper;
    private Context context;

    public BrandDao(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
    }

    /**
     * Create a new brand with logo as BLOB
     * @param brand The brand to create
     * @param logoUri The URI of the logo image (optional)
     * @return The ID of the created brand
     */
    public long createBrand(Brand brand, Uri logoUri) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long brandId = -1;

        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.KEY_NAME, brand.getName());
            values.put(DatabaseHelper.KEY_DESCRIPTION, brand.getDescription());
            values.put(DatabaseHelper.KEY_COUNTRY_ORIGIN, brand.getCountryOfOrigin());
            values.put(DatabaseHelper.KEY_WEBSITE, brand.getWebsite());
            values.put(DatabaseHelper.KEY_IS_ACTIVE, brand.isActive() ? 1 : 0);

            // Convert logo URI to byte array if provided
            if (logoUri != null) {
                byte[] logoBytes = ImageUtils.uriToBlob(context, logoUri);
                values.put(DatabaseHelper.KEY_LOGO, logoBytes);
            } else if (brand.getLogoData() != null) {
                values.put(DatabaseHelper.KEY_LOGO, brand.getLogoData());
            }

            // Insert row
            brandId = db.insert(DatabaseHelper.TABLE_BRANDS, null, values);

            if (brandId != -1) {
                Log.d(TAG, "Brand created with ID: " + brandId);
            } else {
                Log.e(TAG, "Failed to create brand");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating brand", e);
        } finally {
            db.close();
        }

        return brandId;
    }

    /**
     * Get a brand by ID
     * @param id The ID of the brand to get
     * @return The brand
     */
    public Brand getBrandById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Brand brand = null;

        try {
            Cursor cursor = db.query(
                    DatabaseHelper.TABLE_BRANDS,
                    null,
                    DatabaseHelper.KEY_ID + "=?",
                    new String[]{String.valueOf(id)},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                brand = cursorToBrand(cursor);
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting brand by ID: " + id, e);
        } finally {
            db.close();
        }

        return brand;
    }

    /**
     * Get all brands
     * @return A list of all brands
     */
    public List<Brand> getAllBrands() {
        List<Brand> brands = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try {
            Cursor cursor = db.query(
                    DatabaseHelper.TABLE_BRANDS,
                    null,
                    null,
                    null,
                    null, null, DatabaseHelper.KEY_NAME + " ASC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Brand brand = cursorToBrand(cursor);
                    brands.add(brand);
                } while (cursor.moveToNext());

                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting all brands", e);
        } finally {
            db.close();
        }

        return brands;
    }

    /**
     * Get all active brands
     * @return A list of all active brands
     */
    public List<Brand> getActiveBrands() {
        List<Brand> brands = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try {
            Cursor cursor = db.query(
                    DatabaseHelper.TABLE_BRANDS,
                    null,
                    DatabaseHelper.KEY_IS_ACTIVE + "=?",
                    new String[]{"1"},
                    null, null, DatabaseHelper.KEY_NAME + " ASC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Brand brand = cursorToBrand(cursor);
                    brands.add(brand);
                } while (cursor.moveToNext());

                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting active brands", e);
        } finally {
            db.close();
        }

        return brands;
    }

    /**
     * Update a brand
     * @param brand The brand to update
     * @param logoUri The URI of the logo image (optional)
     * @return The number of rows affected
     */
    public int updateBrand(Brand brand, Uri logoUri) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsAffected = 0;

        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.KEY_NAME, brand.getName());
            values.put(DatabaseHelper.KEY_DESCRIPTION, brand.getDescription());
            values.put(DatabaseHelper.KEY_COUNTRY_ORIGIN, brand.getCountryOfOrigin());
            values.put(DatabaseHelper.KEY_WEBSITE, brand.getWebsite());
            values.put(DatabaseHelper.KEY_IS_ACTIVE, brand.isActive() ? 1 : 0);

            // Convert logo URI to byte array if new logo is provided
            if (logoUri != null) {
                byte[] logoBytes = ImageUtils.uriToBlob(context, logoUri);
                values.put(DatabaseHelper.KEY_LOGO, logoBytes);
            } else if (brand.getLogoData() != null) {
                values.put(DatabaseHelper.KEY_LOGO, brand.getLogoData());
            }

            // Update row
            rowsAffected = db.update(
                    DatabaseHelper.TABLE_BRANDS,
                    values,
                    DatabaseHelper.KEY_ID + "=?",
                    new String[]{String.valueOf(brand.getId())});
        } catch (Exception e) {
            Log.e(TAG, "Error updating brand: " + brand.getId(), e);
        } finally {
            db.close();
        }

        return rowsAffected;
    }

    /**
     * Delete a brand
     * @param brandId The ID of the brand to delete
     * @return True if successful, false otherwise
     */
    public boolean deleteBrand(int brandId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean success = false;

        try {
            int rowsAffected = db.delete(
                    DatabaseHelper.TABLE_BRANDS,
                    DatabaseHelper.KEY_ID + "=?",
                    new String[]{String.valueOf(brandId)});

            success = rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting brand: " + brandId, e);
        } finally {
            db.close();
        }

        return success;
    }

    /**
     * Search brands by name
     * @param query The search query
     * @return A list of brands matching the query
     */
    public List<Brand> searchBrands(String query) {
        List<Brand> brands = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            String searchQuery = "%" + query.toLowerCase() + "%";

            cursor = db.query(
                    DatabaseHelper.TABLE_BRANDS,
                    null,
                    "LOWER(" + DatabaseHelper.KEY_NAME + ") LIKE ?",
                    new String[]{searchQuery},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Brand brand = cursorToBrand(cursor);
                    brands.add(brand);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error searching brands with query: " + query, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return brands;
    }

    /**
     * Helper method to convert cursor to Brand object
     */
    private Brand cursorToBrand(Cursor cursor) {
        Brand brand = new Brand();

        int idIndex = cursor.getColumnIndex(DatabaseHelper.KEY_ID);
        if (idIndex >= 0) {
            brand.setId(cursor.getInt(idIndex));
        }

        int nameIndex = cursor.getColumnIndex(DatabaseHelper.KEY_NAME);
        if (nameIndex >= 0) {
            brand.setName(cursor.getString(nameIndex));
        }

        int descriptionIndex = cursor.getColumnIndex(DatabaseHelper.KEY_DESCRIPTION);
        if (descriptionIndex >= 0) {
            brand.setDescription(cursor.getString(descriptionIndex));
        }

        int countryIndex = cursor.getColumnIndex(DatabaseHelper.KEY_COUNTRY_ORIGIN);
        if (countryIndex >= 0) {
            brand.setCountryOfOrigin(cursor.getString(countryIndex));
        }

        int websiteIndex = cursor.getColumnIndex(DatabaseHelper.KEY_WEBSITE);
        if (websiteIndex >= 0) {
            brand.setWebsite(cursor.getString(websiteIndex));
        }

        int activeIndex = cursor.getColumnIndex(DatabaseHelper.KEY_IS_ACTIVE);
        if (activeIndex >= 0) {
            brand.setActive(cursor.getInt(activeIndex) == 1);
        }

        // Get logo blob
        int logoIndex = cursor.getColumnIndex(DatabaseHelper.KEY_LOGO);
        if (logoIndex >= 0) {
            brand.setLogoData(cursor.getBlob(logoIndex));
        }

        return brand;
    }
} 