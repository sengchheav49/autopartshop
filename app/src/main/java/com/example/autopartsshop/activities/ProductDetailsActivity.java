package com.example.autopartsshop.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.bumptech.glide.Glide;
import com.example.autopartsshop.R;
import com.example.autopartsshop.database.CartDao;
import com.example.autopartsshop.database.ProductDao;
import com.example.autopartsshop.models.CartItem;
import com.example.autopartsshop.models.Product;
import com.example.autopartsshop.utils.Constants;
import com.example.autopartsshop.utils.ImageUtils;
import com.example.autopartsshop.utils.SharedPrefManager;

public class ProductDetailsActivity extends AppCompatActivity {

    private ImageView imageViewProductDetail;
    private TextView textViewProductDetailName;
    private TextView textViewProductDetailCategory;
    private TextView textViewProductDetailPrice;
    private TextView textViewProductDetailStock;
    private TextView textViewProductDetailDescription;
    private TextView textViewQuantity;
    private ImageButton buttonDecreaseQuantity;
    private ImageButton buttonIncreaseQuantity;
    private Button buttonAddToCartDetail;

    private ProductDao productDao;
    private CartDao cartDao;
    private SharedPrefManager sharedPrefManager;

    private Product product;
    private int quantity = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        // Initialize DAO and SharedPrefManager
        productDao = new ProductDao(this);
        cartDao = new CartDao(this);
        sharedPrefManager = SharedPrefManager.getInstance(this);

        // Get product ID from intent
        int productId = getIntent().getIntExtra("product_id", -1);

        if (productId == -1) {
            Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Get product from database
        product = productDao.getProductById(productId);

        if (product == null) {
            Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        displayProductDetails();
        setupListeners();
        backButton();
    }

    private void backButton() {
        ImageButton productBackIcon = findViewById(R.id.detailsBackIcon);
        productBackIcon.setOnClickListener(v -> finish()); // Closes the current activity
    }

    private void initViews() {
        imageViewProductDetail = findViewById(R.id.imageViewProductDetail);
        textViewProductDetailName = findViewById(R.id.textViewProductDetailName);
        textViewProductDetailCategory = findViewById(R.id.textViewProductDetailCategory);
        textViewProductDetailPrice = findViewById(R.id.textViewProductDetailPrice);
        textViewProductDetailStock = findViewById(R.id.textViewProductDetailStock);
        textViewProductDetailDescription = findViewById(R.id.textViewProductDetailDescription);
        textViewQuantity = findViewById(R.id.textViewQuantity);
        buttonDecreaseQuantity = findViewById(R.id.buttonDecreaseQuantity);
        buttonIncreaseQuantity = findViewById(R.id.buttonIncreaseQuantity);
        buttonAddToCartDetail = findViewById(R.id.buttonAddToCartDetail);
    }

    private void displayProductDetails() {
        if (product.getImageData() != null) {
            if (product.getImageData() instanceof byte[]) {
                byte[] imageData = (byte[]) product.getImageData();
                Glide.with(this)
                        .asBitmap()
                        .load(imageData)
                        .into(imageViewProductDetail);
            }
        } else {
            Toast.makeText(this, "This product may not available", Toast.LENGTH_SHORT).show();
        }

        // Set text fields
        textViewProductDetailName.setText(product.getName());
        textViewProductDetailCategory.setText(product.getCategory());
        textViewProductDetailPrice.setText(product.getFormattedPrice());
        textViewProductDetailDescription.setText(product.getDescription());

        // Set stock info
        String stockText = product.getStock() + " in stock";
        textViewProductDetailStock.setText(stockText);

        // Set initial quantity
        textViewQuantity.setText(String.valueOf(quantity));
    }

    private void setupListeners() {
        // Decrease quantity button
        buttonDecreaseQuantity.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                textViewQuantity.setText(String.valueOf(quantity));
            }
        });

        // Increase quantity button
        buttonIncreaseQuantity.setOnClickListener(v -> {
            if (quantity < Math.min(product.getStock(), Constants.MAX_QUANTITY)) {
                quantity++;
                textViewQuantity.setText(String.valueOf(quantity));
            } else {
                Toast.makeText(this, "Maximum quantity reached", Toast.LENGTH_SHORT).show();
            }
        });

        // Add to cart button
        buttonAddToCartDetail.setOnClickListener(v -> addToCart());
    }

    private void addToCart() {
        // Check if user is logged in
        if (!sharedPrefManager.isLoggedIn()) {
            Toast.makeText(this, "Please login to add items to cart", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if product is in stock
        if (product.getStock() < quantity) {
            Toast.makeText(this, "Not enough stock available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add to cart
        int userId = sharedPrefManager.getUser().getId();
        CartItem cartItem = new CartItem(userId, product.getId(), quantity);

        long result = cartDao.addToCart(cartItem);

        if (result > 0) {
            Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to add to cart", Toast.LENGTH_SHORT).show();
        }
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