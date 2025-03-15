package com.example.autopartsshop.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.autopartsshop.R;
import com.example.autopartsshop.adapters.CategoryDropdownAdapter;
import com.example.autopartsshop.database.DatabaseHelper;
import com.example.autopartsshop.database.ProductDao;
import com.example.autopartsshop.models.Category;
import com.example.autopartsshop.models.Product;
import com.example.autopartsshop.utils.DrawableUtils;
import com.example.autopartsshop.utils.SharedPrefManager;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class AdminAddProductActivity extends AppCompatActivity {

    private static final String TAG = "AdminAddProductActivity";
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imageViewProductPhoto;
    private ImageButton buttonDrawableImage;
    private EditText editTextProductName;
    private EditText editTextProductDescription;
    private EditText editTextProductPrice;
    private EditText editTextProductStock;
    private AutoCompleteTextView dropdownProductCategory;
    private Button buttonSaveProduct;
    private ProductDao productDao;
    private SharedPrefManager sharedPrefManager;
    private Product product;
    private boolean isEditMode = false;
    private Uri selectedProductImageUri;
    private Uri selectedLogoImageUri;
    private static final int PICK_IMAGE_REQUESTT = 101;
    private static final int PERMISSION_REQUEST_CODE = 102;
    private byte[] selectedDrawableData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_product);
        productDao = new ProductDao(this);
        sharedPrefManager = SharedPrefManager.getInstance(this);

        if (!sharedPrefManager.isLoggedIn() || !sharedPrefManager.isAdmin()) {
            Toast.makeText(this, "Admin access required", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initViews();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        int productId = getIntent().getIntExtra("product_id", -1);
        Log.d(TAG, "Product ID from intent: " + productId);

        if (productId != -1) {
            isEditMode = true;
            product = productDao.getProductById(productId);

            if (product != null) {
                Log.d(TAG, "Editing product: " + product.getName());
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Edit Product");
                }
                fillProductData();
            } else {
                Log.e(TAG, "Product not found with ID: " + productId);
                Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } else {
            Log.d(TAG, "Adding new product");
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Add Product");
            }
        }

        loadCategories();
        setupListeners();
        backButton();
    }

    private void backButton() {
        ImageButton productBackIcon = findViewById(R.id.productBackIcon);
        productBackIcon.setOnClickListener(v -> finish()); // Closes the current activity
    }

    @SuppressLint("WrongViewCast")
    private void initViews() {
        imageViewProductPhoto = findViewById(R.id.imageViewProductPhoto);
        buttonDrawableImage = findViewById(R.id.buttonDrawableImage);
        editTextProductName = findViewById(R.id.editTextProductName);
        editTextProductDescription = findViewById(R.id.editTextProductDescription);
        editTextProductPrice = findViewById(R.id.editTextProductPrice);
        editTextProductStock = findViewById(R.id.editTextProductStock);
        dropdownProductCategory = findViewById(R.id.dropdownProductCategory);
        buttonSaveProduct = findViewById(R.id.buttonSaveProduct);
    }

    private void loadCategories() {
        try {
            List<Category> categories = Category.getCategorys();
            CategoryDropdownAdapter adapter = new CategoryDropdownAdapter(
                    this, R.layout.item_category_drop_down, categories);
            dropdownProductCategory.setAdapter(adapter);
            if (categories.isEmpty()) {
                dropdownProductCategory.setText("Other", false);
            }
            dropdownProductCategory.setOnItemClickListener((parent, view, position, id) -> {
                Category selectedCategory = (Category) parent.getItemAtPosition(position);
                Toast.makeText(this, "Selected: " + selectedCategory.getName(), Toast.LENGTH_SHORT).show();
            });

        } catch (Exception e) {
            Log.e(TAG, "Error loading categories: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading categories: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void fillProductData() {
        try {
            // Set form fields
            editTextProductName.setText(product.getName());
            editTextProductDescription.setText(product.getDescription());
            editTextProductPrice.setText(String.valueOf(product.getPrice()));
            editTextProductStock.setText(String.valueOf(product.getStock()));
            dropdownProductCategory.setText(product.getCategory(), false);

            byte[] imageData = product.getImageData(); // Assuming it returns a byte array
            if (imageData != null && imageData.length > 0) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                imageViewProductPhoto.setImageBitmap(bitmap);
            } else {
                imageViewProductPhoto.setImageResource(R.drawable.ic_add); // Default image
            }

        } catch (Exception e) {
            Log.e(TAG, "Error filling product data: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading product data", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveProduct() {
        try {
            buttonSaveProduct.setEnabled(false); // Disable button to prevent multiple clicks

            // Get input values
            String name = editTextProductName.getText().toString().trim();
            String description = editTextProductDescription.getText().toString().trim();
            String priceString = editTextProductPrice.getText().toString().trim();
            String stockString = editTextProductStock.getText().toString().trim();
            String category = dropdownProductCategory.getText().toString().trim();

            // Convert product image URI to byte array or use drawable data
            byte[] imageData = null;
            if (selectedDrawableData != null) {
                imageData = selectedDrawableData;
                Log.d(TAG, "Using selected drawable data for image");
            } else if (selectedProductImageUri != null) {
                imageData = convertUriToByteArray(selectedProductImageUri);
                Log.d(TAG, "Product image URI: " + selectedProductImageUri.toString());
            } else if (product != null) {
                imageData = product.getImageData();
            }

            byte[] logoImageData = null;
            if (selectedLogoImageUri != null) {
                if (selectedProductImageUri != null && selectedLogoImageUri.equals(selectedProductImageUri)) {
                    Log.e(TAG, "Warning: Product image and logo image are the same URI!");
                    Toast.makeText(this, "Product image and logo image cannot be the same!", Toast.LENGTH_SHORT).show();
                    buttonSaveProduct.setEnabled(true);
                    return;
                }
                logoImageData = convertUriToByteArray(selectedLogoImageUri);
                Log.d(TAG, "Logo image URI: " + selectedLogoImageUri.toString());
            } else if (selectedDrawableData != null) {
                logoImageData = selectedDrawableData;
                Log.d(TAG, "Using selected drawable data for logo");
            } else if (product != null) {
                logoImageData = product.getBrandLogoImageData();
            }

            // Validate inputs
            if (!validateInputs(name, description, priceString, stockString, category, imageData)) {
                buttonSaveProduct.setEnabled(true);
                return;
            }

            // Parse numeric values safely
            double price;
            int stock;
            try {
                price = Double.parseDouble(priceString);
                stock = Integer.parseInt(stockString);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid price or stock value", Toast.LENGTH_SHORT).show();
                buttonSaveProduct.setEnabled(true);
                return;
            }

            if (isEditMode) {
                if (product != null) {
                    product.setName(name);
                    product.setDescription(description);
                    product.setPrice(price);
                    product.setCategory(category);
                    product.setStock(stock);
                    product.setImageData(imageData);
                    product.setBrandLogoImageData(logoImageData);

                    int result = productDao.updateProduct(product, selectedProductImageUri);
                    if (result > 0) {
                        Toast.makeText(this, "Product updated successfully", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to update product", Toast.LENGTH_SHORT).show();
                        buttonSaveProduct.setEnabled(true);
                    }
                } else {
                    Toast.makeText(this, "Product not found for update", Toast.LENGTH_SHORT).show();
                    buttonSaveProduct.setEnabled(true);
                }
            } else {
                Product newProduct = new Product();
                newProduct.setName(name);
                newProduct.setDescription(description);
                newProduct.setPrice(price);
                newProduct.setCategory(category);
                newProduct.setStock(stock);
                newProduct.setImageData(imageData);
                newProduct.setBrandLogoImageData(logoImageData);

                long productId = productDao.insertProduct(newProduct, selectedProductImageUri);
                if (productId > 0) {
                    Toast.makeText(this, "Product added successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(this, "Failed to add product", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving product: " + e.getMessage(), e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            buttonSaveProduct.setEnabled(true);
        }
    }

    private byte[] convertUriToByteArray(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, length);
            }
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            Log.e("ImageConversion", "Error converting URI to byte array", e);
            return null;
        }
    }

    private void setupListeners() {
        // Drawable selection button
        buttonDrawableImage.setOnClickListener(v -> {
            openDrawableSelector();
        });

        // Save button
        buttonSaveProduct.setOnClickListener(v -> {
            saveProduct();
        });
    }

    private void startCrop(Uri sourceUri) {
        Uri destinationUri = Uri.fromFile(new File(getCacheDir(), "cropped_image.jpg"));
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int heightInPx = (int) (250 * getResources().getDisplayMetrics().density);

        UCrop.Options options = new UCrop.Options();
        options.setCompressionQuality(80);
        options.setToolbarTitle("Crop Image");
        options.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.colorSuccess));
        options.setActiveControlsWidgetColor(ContextCompat.getColor(this, R.color.colorPrimary));

        // Set crop area to screen width x 250dp
        UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(screenWidth, heightInPx)  // Set aspect ratio
                .withMaxResultSize(screenWidth, heightInPx)  // Set max crop size
                .withOptions(options)
                .start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == UCrop.REQUEST_CROP) {
                Uri resultUri = UCrop.getOutput(data);
                if (resultUri != null) {
                    selectedProductImageUri = resultUri;
                    imageViewProductPhoto.setImageURI(selectedProductImageUri);
                }
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            Throwable cropError = UCrop.getError(data);
            Toast.makeText(this, "Crop Error: " + (cropError != null ? cropError.getMessage() : "Unknown"), Toast.LENGTH_SHORT).show();
        }
    }

    private void openDrawableSelector() {
        try {
            DrawableUtils.showDrawableSelector(this, (drawableResId, drawableData) -> {
                try {
                    // Store the drawable data for saving later
                    selectedDrawableData = drawableData;
                    selectedProductImageUri = null; // Clear any selected URI since we're using a drawable

                    // Display the selected drawable in the image view
                    if (drawableData != null) {
                        imageViewProductPhoto.setImageResource(drawableResId);
                        Toast.makeText(this, "Car brand selected", Toast.LENGTH_SHORT).show();
                    } else {
                        // If we couldn't get the drawable data, use a default icon
                        imageViewProductPhoto.setImageResource(R.drawable.ic_default_brand);
                        Toast.makeText(this, "Using default image", Toast.LENGTH_SHORT).show();
                    }
                } catch (Resources.NotFoundException e) {
                    Log.e("AdminAddProduct", "Resource not found: " + drawableResId, e);
                    // Use a default icon if the resource is not found
                    imageViewProductPhoto.setImageResource(R.drawable.ic_default_brand);
                    Toast.makeText(this, "Error loading image, using default", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e("AdminAddProduct", "Error setting image: " + e.getMessage(), e);
                    imageViewProductPhoto.setImageResource(R.drawable.ic_default_brand);
                    Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e("AdminAddProduct", "Error showing drawable selector: " + e.getMessage(), e);
            Toast.makeText(this, "Could not open car brand selector", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInputs(String name, String description, String priceString, String stockString, String category, byte[] imageData) {
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Product name is required", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Product description is required", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(priceString)) {
            Toast.makeText(this, "Product price is required", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(stockString)) {
            Toast.makeText(this, "Product stock is required", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(category)) {
            Toast.makeText(this, "Product category is required", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}