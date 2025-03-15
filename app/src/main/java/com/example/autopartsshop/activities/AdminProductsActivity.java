package com.example.autopartsshop.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.autopartsshop.R;
import com.example.autopartsshop.adapters.AdminProductAdapter;
import com.example.autopartsshop.database.ProductDao;
import com.example.autopartsshop.models.Product;
import com.example.autopartsshop.utils.SharedPrefManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class AdminProductsActivity extends AppCompatActivity {

    private static final String TAG = "AdminProductsActivity";

    private RecyclerView recyclerViewAdminProducts;
    private LinearLayout layoutEmpty;
    private SwipeRefreshLayout swipeRefreshLayout;
    private EditText editTextSearch;
    private ChipGroup chipGroupCategories;
    private FloatingActionButton fabAddProduct;

    private ProductDao productDao;
    private SharedPrefManager sharedPrefManager;
    private AdminProductAdapter productAdapter;

    private List<Product> allProducts;
    private List<Product> filteredProducts;
    private String selectedCategory = ""; // Empty means all categories
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_products);
        productDao = new ProductDao(this);
        sharedPrefManager = SharedPrefManager.getInstance(this);

        if (!sharedPrefManager.isLoggedIn() || !sharedPrefManager.isAdmin()) {
            startActivity(new Intent(this, AdminLoginActivity.class));
            finish();
            return;
        }

        initViews();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Manage Products");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        loadCategories();
        loadProducts();
        setupListeners();
        backButton();
    }

    private void backButton() {
        ImageButton productBackIcon = findViewById(R.id.adminBackIcon);
        productBackIcon.setOnClickListener(v -> finish()); // Closes the current activity
    }

    private void initViews() {
        recyclerViewAdminProducts = findViewById(R.id.recyclerViewAdminProducts);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        editTextSearch = findViewById(R.id.editTextSearch);
        chipGroupCategories = findViewById(R.id.chipGroupCategories);
        fabAddProduct = findViewById(R.id.fabAddProduct);
    }

    private void loadCategories() {

        List<String> categories = productDao.getAllCategories();

        Chip chipAll = findViewById(R.id.chipAll);
        chipAll.setOnClickListener(v -> {
            selectedCategory = "";
            filterProducts();
        });

        for (String category : categories) {
            if (category != null && !category.trim().isEmpty()) {
                Chip chip = new Chip(this);
                chip.setText(category);
                chip.setCheckable(true);
                chip.setClickable(true);

                chip.setOnClickListener(v -> {
                    selectedCategory = category;
                    filterProducts();
                });

                chipGroupCategories.addView(chip);
            } else {
                Log.d(TAG, "Skipping empty or null category");
            }
        }
    }

    private void loadProducts() {
        // Show loading indicator
        swipeRefreshLayout.setRefreshing(true);

        // Get all products
        allProducts = productDao.getAllProducts();
        filteredProducts = new ArrayList<>(allProducts);

        // Set up adapter
        productAdapter = new AdminProductAdapter(filteredProducts);
        recyclerViewAdminProducts.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAdminProducts.setAdapter(productAdapter);

        // Handle empty state
        updateEmptyState();

        // Stop loading indicator
        swipeRefreshLayout.setRefreshing(false);

        // Set up adapter callbacks
        setupAdapterCallbacks();
    }

    private void setupAdapterCallbacks() {
        try {
            // Product click listener
            productAdapter.setOnItemClickListener(product -> {
                Log.d(TAG, "Item clicked: " + product.getName());
                // Navigate to product details
                Intent intent = new Intent(AdminProductsActivity.this, AdminAddProductActivity.class);
                intent.putExtra("product_id", product.getId());
                startActivity(intent);
            });

            // Menu click listener
            productAdapter.setOnMenuClickListener((product, view) -> {
                Log.d(TAG, "Menu clicked for: " + product.getName());
                // Show popup menu
                PopupMenu popupMenu = new PopupMenu(this, view);
                popupMenu.getMenuInflater().inflate(R.menu.admin_product_menu, popupMenu.getMenu());

                // Handle menu item clicks
                popupMenu.setOnMenuItemClickListener(item -> {
                    int id = item.getItemId();
                    if (id == R.id.action_edit) {
                        Log.d(TAG, "Edit option selected for: " + product.getName());
                        // Navigate to edit product
                        Intent intent = new Intent(AdminProductsActivity.this, AdminAddProductActivity.class);
                        intent.putExtra("product_id", product.getId());
                        startActivity(intent);
                        return true;
                    } else if (id == R.id.action_delete) {
                        Log.d(TAG, "Delete option selected for: " + product.getName());
                        // Show delete confirmation
                        showDeleteConfirmation(product);
                        return true;
                    }
                    return false;
                });

                popupMenu.show();
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up adapter callbacks: " + e.getMessage(), e);
            Toast.makeText(this, "Error setting up adapter: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmation(Product product) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete \"" + product.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    try {
                        // Delete product
                        productDao.deleteProduct(product.getId());

                        // Remove from lists
                        allProducts.remove(product);
                        filteredProducts.remove(product);

                        // Update adapter
                        productAdapter.notifyDataSetChanged();

                        // Update empty state
                        updateEmptyState();

                        // Show confirmation
                        Toast.makeText(this, "Product deleted successfully", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e(TAG, "Error deleting product: " + e.getMessage(), e);
                        Toast.makeText(this, "Error deleting product: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupListeners() {
        // Swipe refresh
        swipeRefreshLayout.setOnRefreshListener(this::refreshProducts);

        // Search
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().trim().toLowerCase();
                filterProducts();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });

        // Add product button
        fabAddProduct.setOnClickListener(v -> {
            Log.d(TAG, "Add product button clicked");
            startActivity(new Intent(AdminProductsActivity.this, AdminAddProductActivity.class));
        });
    }

    private void refreshProducts() {
        allProducts = productDao.getAllProducts();
        filterProducts();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void filterProducts() {
        filteredProducts.clear();

        String category = selectedCategory != null ? selectedCategory : "";

        for (Product product : allProducts) {
            boolean categoryMatch = category.isEmpty() ||
                    (product.getCategory() != null && product.getCategory().equals(category));

            boolean searchMatch = (searchQuery == null || searchQuery.isEmpty()) ||
                    (product.getName() != null && product.getName().toLowerCase().contains(searchQuery.toLowerCase())) ||
                    (product.getDescription() != null && product.getDescription().toLowerCase().contains(searchQuery.toLowerCase()));

            if (categoryMatch && searchMatch) {
                filteredProducts.add(product);
            }
        }

        productAdapter.notifyDataSetChanged();

        updateEmptyState();
    }

    private void updateEmptyState() {
        if (filteredProducts.isEmpty()) {
            recyclerViewAdminProducts.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerViewAdminProducts.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
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

    @Override
    protected void onResume() {
        super.onResume();
        refreshProducts();
    }

   
}