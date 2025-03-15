package com.example.autopartsshop.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.autopartsshop.R;
import com.example.autopartsshop.activities.ProductDetailsActivity;
import com.example.autopartsshop.activities.SearchResultsActivity;
import com.example.autopartsshop.adapters.BannerAdapter;
import com.example.autopartsshop.adapters.CategoryAdapter;
import com.example.autopartsshop.adapters.ProductAdapter;
import com.example.autopartsshop.database.ProductDao;
import com.example.autopartsshop.models.Banner;
import com.example.autopartsshop.models.Category;
import com.example.autopartsshop.models.Product;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private ViewPager2 viewPagerBanner;
    private WormDotsIndicator dotsIndicator;
    private RecyclerView recyclerViewCategories;
    private RecyclerView recyclerViewPopularProducts;
    private RecyclerView recyclerViewNewProducts;
    private TextView textViewSeeAllPopular;
    private TextView textViewSeeAllNew;
    private EditText editTextSearch;

    private BannerAdapter bannerAdapter;
    private CategoryAdapter categoryAdapter;
    private ProductAdapter popularProductsAdapter;
    private ProductAdapter newProductsAdapter;

    private ProductDao productDao;
    private List<Banner> bannerList;
    private List<Category> categoryList;
    private List<Product> popularProductsList;
    private List<Product> newProductsList;

    private final Handler autoScrollHandler = new Handler(Looper.getMainLooper());
    private final Runnable autoScrollRunnable = new Runnable() {
        @Override
        public void run() {
            if (viewPagerBanner == null) return;

            int currentPosition = viewPagerBanner.getCurrentItem();
            int totalCount = bannerAdapter.getItemCount();

            if (currentPosition < totalCount - 1) {
                viewPagerBanner.setCurrentItem(currentPosition + 1);
            } else {
                viewPagerBanner.setCurrentItem(0);
            }

            autoScrollHandler.postDelayed(this, 3000); // Auto scroll every 3 seconds
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        productDao = new ProductDao(requireContext());
        initViews(view);
        setupBanners();
        setupPopularProducts();
        setupNewProducts();
        setupCategories();
        setupListeners();
    }

    private void initViews(View view) {
        viewPagerBanner = view.findViewById(R.id.viewPagerBanner);
        dotsIndicator = view.findViewById(R.id.dotsIndicator);
        recyclerViewCategories = view.findViewById(R.id.recyclerViewCategories);
        recyclerViewPopularProducts = view.findViewById(R.id.recyclerViewPopularProducts);
        recyclerViewNewProducts = view.findViewById(R.id.recyclerViewNewProducts);
        textViewSeeAllPopular = view.findViewById(R.id.textViewSeeAllPopular);
        textViewSeeAllNew = view.findViewById(R.id.textViewSeeAllNew);
        editTextSearch = view.findViewById(R.id.editTextSearch);
    }

    private void setupBanners() {
        bannerList = new ArrayList<>();
        bannerList.add(new Banner(R.drawable.logo_loading, "Summer Sale", "Up to 50% off on all car parts"));
        bannerList.add(new Banner(R.drawable.bmwm4, "New Arrivals", "Check out our latest products"));
        bannerList.add(new Banner(R.drawable.cr, "Premium Deals", "High-quality parts at affordable prices"));
        
        // Ensure only 3 banners are shown
        if (bannerList.size() > 3) {
            bannerList = bannerList.subList(0, 3);
        }
        
        bannerAdapter = new BannerAdapter(bannerList);
        viewPagerBanner.setAdapter(bannerAdapter);
        dotsIndicator.setViewPager2(viewPagerBanner);
        startAutoScroll();
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


    }

    private void setupPopularProducts() {
        popularProductsList = productDao.getAllProducts();
        
        // Limit to only 3 popular products
        if (popularProductsList.size() > 3) {
            popularProductsList = popularProductsList.subList(0, 3);
        }
        
        popularProductsAdapter = new ProductAdapter(popularProductsList);
        recyclerViewPopularProducts.setLayoutManager(new LinearLayoutManager(requireContext(),
                LinearLayoutManager.HORIZONTAL, false));
        recyclerViewPopularProducts.setAdapter(popularProductsAdapter);
        popularProductsAdapter.setOnItemClickListener(product -> {
            Intent intent = new Intent(requireContext(), ProductDetailsActivity.class);
            intent.putExtra("product_id", product.getId());
            startActivity(intent);
        });
    }

    private void setupNewProducts() {
        newProductsList = productDao.getAllProducts();
        newProductsAdapter = new ProductAdapter(newProductsList);
        recyclerViewNewProducts.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        recyclerViewNewProducts.setAdapter(newProductsAdapter);

        newProductsAdapter.setOnItemClickListener(product -> {
            Intent intent = new Intent(requireContext(), ProductDetailsActivity.class);
            intent.putExtra("product_id", product.getId());
            startActivity(intent);
        });
    }

    private void setupListeners() {
        editTextSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });

    }

    /**
     * Perform search based on user input
     */
    // In HomeFragment.java
    private void performSearch() {
        String query = editTextSearch.getText().toString().trim();
        if (!query.isEmpty()) {
            Intent intent = new Intent(requireContext(), SearchResultsActivity.class);
            intent.putExtra("query", query);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }


    private void startAutoScroll() {
        autoScrollHandler.postDelayed(autoScrollRunnable, 3000);
    }

    private void stopAutoScroll() {
        autoScrollHandler.removeCallbacks(autoScrollRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        startAutoScroll();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopAutoScroll();
    }
}