package com.example.autopartsshop.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriPermission;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import androidx.core.content.FileProvider;
import android.util.Log;

import com.example.autopartsshop.models.Brand;
import com.example.autopartsshop.models.Product;
import com.example.autopartsshop.utils.ImageUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.nio.charset.StandardCharsets;
import org.json.JSONException;
import org.json.JSONObject;

public class ProductDao {
    private static final String TAG = "ProductDao";
    private DatabaseHelper dbHelper;
    private Context context;

    private SQLiteDatabase db;
    public ProductDao(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
    }

    /**
     * Create a new product
     */
//    public long createProduct(Product product) {
//        return createProduct(product, null);
//    }

    /**
     * Create a new product with image URI
     */
    public long createProduct(Product product, Uri imageUri) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long productId = -1;

        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.KEY_NAME, product.getName());
            values.put(DatabaseHelper.KEY_BRAND_NAME,product.getBrandName());
            values.put(DatabaseHelper.KEY_DESCRIPTION, product.getDescription());
            values.put(DatabaseHelper.KEY_PRICE, product.getPrice());
            values.put(DatabaseHelper.KEY_CATEGORY, product.getCategory());
            values.put(DatabaseHelper.KEY_STOCK, product.getStock());

            // Convert image URI to byte array if provided
            if (imageUri != null) {
                byte[] imageBytes = ImageUtils.convertImageUriToByteArray(context, imageUri);
                values.put(DatabaseHelper.KEY_IMAGE, imageBytes);
            } else {
                values.put(DatabaseHelper.KEY_IMAGE, product.getImageData()); // Use getImageData() instead of setImageData()
            }

            if (imageUri != null) {
                byte[] imageBytes = ImageUtils.convertImageUriToByteArray(context, imageUri);
                values.put(DatabaseHelper.KEY_LOGO_IMAGE, imageBytes);
            } else {
                values.put(DatabaseHelper.KEY_LOGO_IMAGE, product.getImageData()); // Use getImageData() instead of setImageData()
            }

            // Handle other BLOB data if provided
            if (product.getTechnicalSpecsData() != null) {
                values.put(DatabaseHelper.KEY_TECHNICAL_SPECS, product.getTechnicalSpecsData());
            }
            
            if (product.getVideoThumbnailData() != null) {
                values.put(DatabaseHelper.KEY_VIDEO_THUMBNAIL, product.getVideoThumbnailData());
            }
            
            if (product.getAdditionalImagesData() != null) {
                values.put(DatabaseHelper.KEY_ADDITIONAL_IMAGES, product.getAdditionalImagesData());
            }
            
            if (product.getManualData() != null) {
                values.put(DatabaseHelper.KEY_MANUAL, product.getManualData());
            }
            
            // Set year and mileage if available
            if (product.getYear() > 0) {
                values.put(DatabaseHelper.KEY_YEAR, product.getYear());
            }
            
            if (product.getMileage() > 0) {
                values.put(DatabaseHelper.KEY_MILEAGE, product.getMileage());
            }

            // If the product has a brand ID, add it to the values
            if (product.getBrandId() > 0) {
                values.put(DatabaseHelper.KEY_BRAND_ID, product.getBrandId());
            }

            // Insert row
            productId = db.insert(DatabaseHelper.TABLE_PRODUCTS, null, values);

            if (productId != -1) {
                Log.d(TAG, "Product created with ID: " + productId);
            } else {
                Log.e(TAG, "Failed to create product");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating product", e);
        } finally {
            db.close();
        }

        return productId;
    }
    public long insertProduct(Product product, Uri imageUri) {
        return createProduct(product, imageUri);
    }

    /**
     * Get a product by ID
     */
    public Product getProductById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Product product = null;

        try {
            Cursor cursor = db.query(
                    DatabaseHelper.TABLE_PRODUCTS,
                    null,
                    DatabaseHelper.KEY_ID + "=?",
                    new String[]{String.valueOf(id)},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                product = cursorToProduct(cursor);
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting product by ID: " + id, e);
        } finally {
            db.close();
        }

        return product;
    }

    /**
     * Get all products
     */
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try {
            Cursor cursor = db.query(
                    DatabaseHelper.TABLE_PRODUCTS,
                    null,
                    null,
                    null,
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Product product = cursorToProduct(cursor);
                    products.add(product);
                } while (cursor.moveToNext());

                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting all products", e);
        } finally {
            db.close();
        }

        return products;
    }

    /**
     * Get products by category
     */
    public List<Product> getProductsByBrandName(String category) {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try {
            Cursor cursor = db.query(
                    DatabaseHelper.TABLE_PRODUCTS,
                    null,
                    DatabaseHelper.KEY_CATEGORY + "=?",
                    new String[]{category},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Product product = cursorToProduct(cursor);
                    products.add(product);
                } while (cursor.moveToNext());

                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting products by category: " + category, e);
        } finally {
            db.close();
        }

        return products;
    }


    @SuppressLint("Range")
    /**
     * Get all unique categories
     */
    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try {
            Cursor cursor = db.query(
                    true,  // distinct
                    DatabaseHelper.TABLE_PRODUCTS,
                    new String[]{DatabaseHelper.KEY_CATEGORY},
                    null, null, null, null,
                    DatabaseHelper.KEY_CATEGORY + " ASC",
                    null);

            if (cursor != null && cursor.moveToFirst()) {
                // Get the index for the category column
                int categoryIndex = cursor.getColumnIndex(DatabaseHelper.KEY_CATEGORY);
                if (categoryIndex != -1) { // Ensure the column exists
                    do {
                        String category = cursor.getString(categoryIndex);
                        categories.add(category);
                    } while (cursor.moveToNext());
                } else {
                    Log.e(TAG, "Category column not found");
                }

                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting all categories", e);
        } finally {
            db.close();
        }

        return categories;
    }

    /**
     * Update a product
     */
    public int updateProduct(Product product, Uri imageUri) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsAffected = 0;

        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.KEY_NAME, product.getName());
            values.put(DatabaseHelper.KEY_BRAND_NAME,product.getBrandName());
            values.put(DatabaseHelper.KEY_DESCRIPTION, product.getDescription());
            values.put(DatabaseHelper.KEY_PRICE, product.getPrice());
            values.put(DatabaseHelper.KEY_CATEGORY, product.getCategory());
            values.put(DatabaseHelper.KEY_STOCK, product.getStock());

            // Convert image URI to byte array if new image is provided
            if (imageUri != null) {
                byte[] imageBytes = ImageUtils.convertImageUriToByteArray(context, imageUri);
                values.put(DatabaseHelper.KEY_IMAGE, imageBytes);
            } else if (product.getImageData() != null) {
                values.put(DatabaseHelper.KEY_IMAGE, product.getImageData());
            }

            if (imageUri != null) {
                byte[] imageBytes = ImageUtils.convertImageUriToByteArray(context, imageUri);
                values.put(DatabaseHelper.KEY_LOGO_IMAGE, imageBytes);
            } else if (product.getImageData() != null) {
                values.put(DatabaseHelper.KEY_LOGO_IMAGE, product.getImageData());
            }

            // Handle other BLOB data if provided
            if (product.getTechnicalSpecsData() != null) {
                values.put(DatabaseHelper.KEY_TECHNICAL_SPECS, product.getTechnicalSpecsData());
            }
            
            if (product.getVideoThumbnailData() != null) {
                values.put(DatabaseHelper.KEY_VIDEO_THUMBNAIL, product.getVideoThumbnailData());
            }
            
            if (product.getAdditionalImagesData() != null) {
                values.put(DatabaseHelper.KEY_ADDITIONAL_IMAGES, product.getAdditionalImagesData());
            }
            
            if (product.getManualData() != null) {
                values.put(DatabaseHelper.KEY_MANUAL, product.getManualData());
            }
            
            // Set year and mileage if available
            if (product.getYear() > 0) {
                values.put(DatabaseHelper.KEY_YEAR, product.getYear());
            }
            
            if (product.getMileage() > 0) {
                values.put(DatabaseHelper.KEY_MILEAGE, product.getMileage());
            }

            // If the product has a brand ID, add it to the values
            if (product.getBrandId() > 0) {
                values.put(DatabaseHelper.KEY_BRAND_ID, product.getBrandId());
            }

            // Update row
            rowsAffected = db.update(
                    DatabaseHelper.TABLE_PRODUCTS,
                    values,
                    DatabaseHelper.KEY_ID + "=?",
                    new String[]{String.valueOf(product.getId())});
        } catch (Exception e) {
            Log.e(TAG, "Error updating product: " + product.getId(), e);
        } finally {
            db.close();
        }

        return rowsAffected;
    }
    /**
     * Delete a product
     */
    public boolean deleteProduct(int productId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean success = false;

        try {
            int rowsAffected = db.delete(
                    DatabaseHelper.TABLE_PRODUCTS,
                    DatabaseHelper.KEY_ID + "=?",
                    new String[]{String.valueOf(productId)});

            success = rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting product: " + productId, e);
        } finally {
            db.close();
        }

        return success;
    }

    /**
     * Search products by name or description
     */
    public List<Product> searchProducts(String query) {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try {
            String searchQuery = "%" + query.toLowerCase() + "%";

            Cursor cursor = db.query(
                    DatabaseHelper.TABLE_PRODUCTS,
                    null,
                    "LOWER(" + DatabaseHelper.KEY_NAME + ") LIKE ? OR " +
                            "LOWER(" + DatabaseHelper.KEY_DESCRIPTION + ") LIKE ?",
                    new String[]{searchQuery, searchQuery},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Product product = cursorToProduct(cursor);
                    products.add(product);
                } while (cursor.moveToNext());

                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error searching products for: " + query, e);
        } finally {
            db.close();
        }

        return products;
    }

    /**
     * Update product stock
     */
    public boolean updateStock(int productId, int newStock) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean success = false;

        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.KEY_STOCK, newStock);

            int rowsAffected = db.update(
                    DatabaseHelper.TABLE_PRODUCTS,
                    values,
                    DatabaseHelper.KEY_ID + "=?",
                    new String[]{String.valueOf(productId)});

            success = rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error updating stock for product: " + productId, e);
        } finally {
            db.close();
        }

        return success;
    }

    /**
     * Helper method to convert cursor to Product object
     */
    private Product cursorToProduct(Cursor cursor) {
        Product product = new Product();

        // Get column indexes safely, checking that they are valid
        int idIndex = cursor.getColumnIndex(DatabaseHelper.KEY_ID);
        if (idIndex >= 0) {
            product.setId(cursor.getInt(idIndex));
        }

        int nameIndex = cursor.getColumnIndex(DatabaseHelper.KEY_NAME);
        if (nameIndex >= 0) {
            product.setName(cursor.getString(nameIndex));
        }

        int brandIndex = cursor.getColumnIndex(DatabaseHelper.KEY_BRAND_NAME);
        if (brandIndex >= 0) {
            product.setBrandName(cursor.getString(brandIndex));
        }

        int descriptionIndex = cursor.getColumnIndex(DatabaseHelper.KEY_DESCRIPTION);
        if (descriptionIndex >= 0) {
            product.setDescription(cursor.getString(descriptionIndex));
        }

        int priceIndex = cursor.getColumnIndex(DatabaseHelper.KEY_PRICE);
        if (priceIndex >= 0) {
            product.setPrice(cursor.getDouble(priceIndex));
        }

        int categoryIndex = cursor.getColumnIndex(DatabaseHelper.KEY_CATEGORY);
        if (categoryIndex >= 0) {
            product.setCategory(cursor.getString(categoryIndex));
        }

        // Handle image column (stored as string)
        // Handle image column (stored as BLOB)
        int imageIndex = cursor.getColumnIndex(DatabaseHelper.KEY_IMAGE);
        if (imageIndex >= 0) {
            byte[] imageData = cursor.getBlob(imageIndex); // Retrieve as byte array
            product.setImageData(imageData); // Now correctly passing byte[]
        }

        int logoIndex = cursor.getColumnIndex(DatabaseHelper.KEY_LOGO_IMAGE);
        if (logoIndex >= 0) {
            byte[] imageData = cursor.getBlob(logoIndex); // Retrieve as byte array
            product.setImageData(imageData); // Now correctly passing byte[]
        }

        int stockIndex = cursor.getColumnIndex(DatabaseHelper.KEY_STOCK);
        if (stockIndex >= 0) {
            product.setStock(cursor.getInt(stockIndex));
        }

        // Set created_at if it exists in the cursor
        int createdAtIndex = cursor.getColumnIndex(DatabaseHelper.KEY_CREATED_AT);
        if (createdAtIndex >= 0) {
            product.setCreatedAt(cursor.getString(createdAtIndex));
        }

        // Get additional BLOB data
        if (cursor.getColumnIndex(DatabaseHelper.KEY_TECHNICAL_SPECS) != -1) {
            product.setTechnicalSpecsData(cursor.getBlob(cursor.getColumnIndex(DatabaseHelper.KEY_TECHNICAL_SPECS)));
        }
        
        if (cursor.getColumnIndex(DatabaseHelper.KEY_VIDEO_THUMBNAIL) != -1) {
            product.setVideoThumbnailData(cursor.getBlob(cursor.getColumnIndex(DatabaseHelper.KEY_VIDEO_THUMBNAIL)));
        }
        
        if (cursor.getColumnIndex(DatabaseHelper.KEY_ADDITIONAL_IMAGES) != -1) {
            product.setAdditionalImagesData(cursor.getBlob(cursor.getColumnIndex(DatabaseHelper.KEY_ADDITIONAL_IMAGES)));
        }
        
        if (cursor.getColumnIndex(DatabaseHelper.KEY_MANUAL) != -1) {
            product.setManualData(cursor.getBlob(cursor.getColumnIndex(DatabaseHelper.KEY_MANUAL)));
        }
        
        // Get year and mileage
        if (cursor.getColumnIndex(DatabaseHelper.KEY_YEAR) != -1) {
            product.setYear(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.KEY_YEAR)));
        }
        
        if (cursor.getColumnIndex(DatabaseHelper.KEY_MILEAGE) != -1) {
            product.setMileage(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.KEY_MILEAGE)));
        }

        // Read JSON data stored in a BLOB
        byte[] specData = product.getTechnicalSpecsData();
        if (specData != null) {
            String jsonString = new String(specData, StandardCharsets.UTF_8);
            try {
                JSONObject specifications = new JSONObject(jsonString);
                String engine = specifications.getString("engine");
                int horsepower = specifications.getInt("horsepower");
                // Use the data...
            } catch (JSONException e) {
                Log.e("ProductDetails", "Error parsing JSON", e);
            }
        }

        return product;
    }

    /**
     * Save technical specifications document (PDF) to a product
     * @param productId The ID of the product
     * @param techSpecsData The PDF data as a byte array
     * @return True if successful, false otherwise
     */
    public boolean saveTechnicalSpecs(int productId, byte[] techSpecsData) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean success = false;
        
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.KEY_TECHNICAL_SPECS, techSpecsData);
            
            int rowsAffected = db.update(
                    DatabaseHelper.TABLE_PRODUCTS,
                    values,
                    DatabaseHelper.KEY_ID + "=?",
                    new String[]{String.valueOf(productId)}
            );
            
            success = rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error saving technical specs", e);
        } finally {
            db.close();
        }
        
        return success;
    }
    
    /**
     * Save manual document (PDF) to a product
     * @param productId The ID of the product
     * @param manualData The PDF data as a byte array
     * @return True if successful, false otherwise
     */
    public boolean saveManual(int productId, byte[] manualData) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean success = false;
        
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.KEY_MANUAL, manualData);
            
            int rowsAffected = db.update(
                    DatabaseHelper.TABLE_PRODUCTS,
                    values,
                    DatabaseHelper.KEY_ID + "=?",
                    new String[]{String.valueOf(productId)}
            );
            
            success = rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error saving manual", e);
        } finally {
            db.close();
        }
        
        return success;
    }
    
    /**
     * Save multiple images for a product
     * @param productId The ID of the product
     * @param imagesData The images data as a byte array
     * @return True if successful, false otherwise
     */
    public boolean saveAdditionalImages(int productId, byte[] imagesData) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean success = false;
        
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.KEY_ADDITIONAL_IMAGES, imagesData);
            
            int rowsAffected = db.update(
                    DatabaseHelper.TABLE_PRODUCTS,
                    values,
                    DatabaseHelper.KEY_ID + "=?",
                    new String[]{String.valueOf(productId)}
            );
            
            success = rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error saving additional images", e);
        } finally {
            db.close();
        }
        
        return success;
    }
    
    /**
     * Save video thumbnail for a product
     * @param productId The ID of the product
     * @param thumbnailData The thumbnail data as a byte array
     * @return True if successful, false otherwise
     */
    public boolean saveVideoThumbnail(int productId, byte[] thumbnailData) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean success = false;
        
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.KEY_VIDEO_THUMBNAIL, thumbnailData);
            
            int rowsAffected = db.update(
                    DatabaseHelper.TABLE_PRODUCTS,
                    values,
                    DatabaseHelper.KEY_ID + "=?",
                    new String[]{String.valueOf(productId)}
            );
            
            success = rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error saving video thumbnail", e);
        } finally {
            db.close();
        }
        
        return success;
    }
    
    /**
     * Example of how to convert blob to base64 for network transmission and back
     * @param imageData The image data as a byte array
     * @return The image data as a byte array (after conversion to base64 and back)
     */
    public byte[] exampleBlobToBase64AndBack(byte[] imageData) {
        // Convert blob to base64 for network transmission
        String base64Data = ImageUtils.blobToBase64(imageData);
        // And back to blob
        return ImageUtils.base64ToBlob(base64Data);
    }

    // Combine multiple images into a single BLOB
    public void saveAdditionalImages(int productId, List<Uri> imageUris) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            List<byte[]> productImages = new ArrayList<>();
            // Add multiple images to the list
            for (Uri imageUri : imageUris) {
                productImages.add(ImageUtils.uriToBlob(context, imageUri));
            }

            // Serialize the list to a single byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutput = new ObjectOutputStream(outputStream);
            objectOutput.writeObject(productImages);
            byte[] combinedImageData = outputStream.toByteArray();

            // Save the combined images
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.KEY_ADDITIONAL_IMAGES, combinedImageData);
            db.update(
                    DatabaseHelper.TABLE_PRODUCTS,
                    values,
                    DatabaseHelper.KEY_ID + "=?",
                    new String[]{String.valueOf(productId)}
            );
        } catch (Exception e) {
            Log.e(TAG, "Error saving additional images", e);
        } finally {
            db.close();
        }
    }

    // Open a PDF stored as a BLOB
    public void openManual(Product product) {
        byte[] pdfData = product.getManualData();
        if (pdfData != null) {
            // Save the BLOB temporarily to a file to open it
            File tempFile = new File(context.getCacheDir(), "product_manual.pdf");
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(pdfData);
                
                // Create an intent to open the PDF
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri fileUri = FileProvider.getUriForFile(context, 
                    "com.example.autopartsshop.fileprovider", tempFile);
                intent.setDataAndType(fileUri, "application/pdf");
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(intent);
            } catch (IOException e) {
                Log.e("ProductDetails", "Error opening PDF", e);
            }
        }
    }

    /**
     * Get products by brand ID
     * @param brandId The ID of the brand to filter by
     * @return A list of products for the specified brand
     */
    public List<Product> getProductsByBrandId(int brandId) {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try {
            Cursor cursor = db.query(
                    DatabaseHelper.TABLE_PRODUCTS,
                    null,
                    DatabaseHelper.KEY_BRAND_ID + "=?",
                    new String[]{String.valueOf(brandId)},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Product product = cursorToProduct(cursor);
                    products.add(product);
                } while (cursor.moveToNext());

                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting products by brand ID: " + brandId, e);
        } finally {
            db.close();
        }

        return products;
    }

    /**
     * Update the brand ID for a product
     * @param productId The ID of the product to update
     * @param brandId The new brand ID
     * @return True if successful, false otherwise
     */
    public boolean updateProductBrand(int productId, int brandId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean success = false;

        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.KEY_BRAND_ID, brandId);

            int rowsAffected = db.update(
                    DatabaseHelper.TABLE_PRODUCTS,
                    values,
                    DatabaseHelper.KEY_ID + "=?",
                    new String[]{String.valueOf(productId)});

            success = rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error updating product brand: " + productId, e);
        } finally {
            db.close();
        }

        return success;
    }

    /**
     * Add a logo to a brand
     * @param brandId The ID of the brand
     * @param logoUri The URI of the logo image
     */
    public void addBrandLogo(int brandId, Uri logoUri) {
        // Get the brand from the database
        BrandDao brandDao = new BrandDao(context);
        Brand brand = brandDao.getBrandById(brandId);
        
        if (brand != null) {
            // Update the brand with the new logo
            brandDao.updateBrand(brand, logoUri);
        }
    }

    /**
     * Update products with brand IDs
     * @param brandName The name of the brand to match
     */
    public void updateProductBrands(String brandName) {
        // Example: Update Toyota products
        List<Product> brandProducts = getProductsByBrandName(brandName);
        BrandDao brandDao = new BrandDao(context);
        List<Brand> brands = brandDao.getAllBrands();
        
        // Find the brand
        Brand targetBrand = null;
        for (Brand brand : brands) {
            if (brandName.equals(brand.getName())) {
                targetBrand = brand;
                break;
            }
        }
        
        if (targetBrand != null) {
            for (Product product : brandProducts) {
                updateProductBrand(product.getId(), targetBrand.getId());
            }
        }
    }
}
