package com.example.autopartsshop.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.autopartsshop.R;
import com.example.autopartsshop.database.ProductDao;
import com.example.autopartsshop.models.User;
import com.example.autopartsshop.utils.SharedPrefManager;

import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView textViewAdminWelcome;
    private TextView textViewTotalProducts;
    private TextView textViewTotalCategories;
    private TextView textViewLowStockProducts;
    private Button buttonManageProducts;
    private Button buttonAdminLogout;
    private CardView cardViewAddProduct;

    private ProductDao productDao;
    private SharedPrefManager sharedPrefManager;
    private User user;

    // Define low stock threshold
    private static final int LOW_STOCK_THRESHOLD = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialize DAOs and SharedPrefManager
        productDao = new ProductDao(this);
        sharedPrefManager = SharedPrefManager.getInstance(this);

        // Check if admin is logged in
        if (!sharedPrefManager.isLoggedIn() || !sharedPrefManager.isAdmin()) {
            startActivity(new Intent(this, AdminLoginActivity.class));
            finish();
            return;
        }

        // Get logged in user
        user = sharedPrefManager.getUser();

        // Initialize views
        initViews();

        // Set up toolbar


        // Display welcome message
        String welcomeMessage = getString(R.string.admin_welcome) + ", " + user.getUsername() + "!";
        textViewAdminWelcome.setText(welcomeMessage);

        // Load dashboard statistics
        loadDashboardStats();

        // Set up listeners
        setupListeners();
    }

    private void initViews() {
        textViewAdminWelcome = findViewById(R.id.textViewAdminWelcome);
        textViewTotalProducts = findViewById(R.id.textViewTotalProducts);
        textViewTotalCategories = findViewById(R.id.textViewTotalCategories);
        textViewLowStockProducts = findViewById(R.id.textViewLowStockProducts);
        buttonManageProducts = findViewById(R.id.buttonManageProducts);
        buttonAdminLogout = findViewById(R.id.buttonAdminLogout);
        cardViewAddProduct = findViewById(R.id.cardViewAddProduct);
    }

    private void loadDashboardStats() {
        // Get all products
        List<String> categories = productDao.getAllCategories();
        List<com.example.autopartsshop.models.Product> products = productDao.getAllProducts();

        // Count low stock products
        int lowStockCount = 0;
        for (com.example.autopartsshop.models.Product product : products) {
            if (product.getStock() <= LOW_STOCK_THRESHOLD) {
                lowStockCount++;
            }
        }

        // Display stats
        textViewTotalProducts.setText(String.valueOf(products.size()));
        textViewTotalCategories.setText(String.valueOf(categories.size()));
        textViewLowStockProducts.setText(String.valueOf(lowStockCount));
    }

    private void setupListeners() {
        // Manage products button
        buttonManageProducts.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, AdminProductsActivity.class));
        });

        // Add product card
        cardViewAddProduct.setOnClickListener(v -> {
            // Replace AdminAddProductActivity with an existing activity or create a new one
            startActivity(new Intent(AdminDashboardActivity.this, MainActivity.class));
        });

        // Logout button
        buttonAdminLogout.setOnClickListener(v -> {
            sharedPrefManager.logout();
            startActivity(new Intent(AdminDashboardActivity.this, LoginActivity.class));
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload stats when returning to dashboard
        loadDashboardStats();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_dashboard_menu, menu);
        return true;
    }



}