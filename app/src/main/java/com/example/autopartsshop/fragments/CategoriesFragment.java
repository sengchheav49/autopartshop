package com.example.autopartsshop.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.autopartsshop.R;
import com.example.autopartsshop.activities.ProductDetailsActivity;
import com.example.autopartsshop.activities.SearchResultsActivity;
import com.example.autopartsshop.adapters.CategoryAdapter;
import com.example.autopartsshop.adapters.ProductAdapter;
import com.example.autopartsshop.adapters.ProductImageAdapter;
import com.example.autopartsshop.database.ProductDao;
import com.example.autopartsshop.models.Category;
import com.example.autopartsshop.models.Product;
import com.example.autopartsshop.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class CategoriesFragment extends Fragment {

    private EditText editTextSearch;
    private RecyclerView recyclerViewCategories;
    private RecyclerView recyclerViewCategoryProducts;
    private TextView textViewCategoryTitle;
    private TextView textViewNoProducts;
    private CategoryAdapter categoryAdapter;
    private ProductAdapter productAdapter;
    private ProductDao productDao;
    private List<Product> filteredProducts;
    private List<Category> categoryList;
    private List<Product> productList;
    private String selectedCategory = "";
    private String searchQuery; // To hold the search query
    private List<Product> allProducts; // To hold all products

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_categories, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        productDao = new ProductDao(requireContext());

        // Initialize views
        initViews(view);

        // Set up search functionality
        setupSearch();

        // Set up data
        setupCategories();

        // Set up RecyclerView for categories
        setupCategoryRecyclerView();

        // Default to showing all products
        showAllProducts();
    }

    private void initViews(View view) {
        editTextSearch = view.findViewById(R.id.editTextSearch);
        recyclerViewCategories = view.findViewById(R.id.recyclerViewCategories);
        recyclerViewCategoryProducts = view.findViewById(R.id.recyclerViewCategoryProducts);
        textViewCategoryTitle = view.findViewById(R.id.textViewCategoryTitle);
        textViewNoProducts = view.findViewById(R.id.textViewNoProducts);
    }

    private void setupSearch() {
        editTextSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                performSearch();
                return true;
            }
            return false;
        });
    }

    private void performSearch() {
        String query = editTextSearch.getText().toString().trim();
        if (!query.isEmpty()) {
            Intent intent = new Intent(requireContext(), SearchResultsActivity.class);
            intent.putExtra("query", query);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else {
            Toast.makeText(requireContext(), "Please enter a search term", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupCategories() {
        List<String> categoryNames = productDao.getAllCategories();
        List<Category> predefinedCategories = Category.getCategorys();

        categoryList = new ArrayList<>();
        categoryList.add(new Category("All", null)); // "All" category with no image

        for (String categoryName : categoryNames) {
            String logoUrl = null;

            for (Category predefined : predefinedCategories) {
                if (predefined.getName().equalsIgnoreCase(categoryName)) {
                    logoUrl = predefined.getIconResource();
                    break;
                }
            }

            categoryList.add(new Category(categoryName, logoUrl));
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewCategories.setLayoutManager(layoutManager);

        categoryAdapter = new CategoryAdapter(categoryList);
        recyclerViewCategories.setAdapter(categoryAdapter);

        categoryAdapter.setOnItemClickListener(category -> {
            if ("All".equals(category.getName())) {
                selectedCategory = "";
                showAllProducts();
            } else {
                selectedCategory = category.getName();
                showCategoryProducts(category.getName());
            }
        });
    }

    private void setupCategoryRecyclerView() {
        List<Product> productList = productDao.getProductsByBrandName(selectedCategory);
        List<Bitmap> imageList = new ArrayList<>();
        for (Product product : productList) {
            byte[] logoImage = product.getBrandLogoImageData();
            if (logoImage != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(logoImage, 0, logoImage.length);
                imageList.add(bitmap);
            }
        }

        recyclerViewCategoryProducts.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        ProductImageAdapter imageAdapter = new ProductImageAdapter(imageList);
        recyclerViewCategoryProducts.setAdapter(imageAdapter);
    }


    private void showAllProducts() {
        textViewCategoryTitle.setText("All Products");
        productList = productDao.getAllProducts();
        updateProductDisplay();
    }


    private void showCategoryProducts(String category) {
        textViewCategoryTitle.setText(category);
        productList = productDao.getProductsByBrandName(category);
        updateProductDisplay();
    }

    private void updateProductDisplay() {
        if (productList.isEmpty()) {
            recyclerViewCategoryProducts.setVisibility(View.GONE);
            textViewNoProducts.setVisibility(View.VISIBLE);
        } else {
            recyclerViewCategoryProducts.setVisibility(View.VISIBLE);
            textViewNoProducts.setVisibility(View.GONE);

            GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 2);
            recyclerViewCategoryProducts.setLayoutManager(layoutManager);

            productAdapter = new ProductAdapter(productList);
            recyclerViewCategoryProducts.setAdapter(productAdapter);

            productAdapter.setOnItemClickListener(product -> {
                // Navigate to product details
                Intent intent = new Intent(requireContext(), ProductDetailsActivity.class);
                intent.putExtra("product_id", product.getId());
                startActivity(intent);
            });
        }
    }

    /**
     * Get appropriate icon for a category
     */


    @Override
    public void onResume() {
        super.onResume();
        if (selectedCategory.isEmpty()) {
            showAllProducts();
        } else {
            showCategoryProducts(selectedCategory);
        }
    }
}