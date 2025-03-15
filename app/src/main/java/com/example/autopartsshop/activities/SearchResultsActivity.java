package com.example.autopartsshop.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.autopartsshop.R;
import com.example.autopartsshop.adapters.ProductAdapter;
import com.example.autopartsshop.database.ProductDao;
import com.example.autopartsshop.models.Product;

import java.util.List;

public class SearchResultsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewSearchResults;
    private LinearLayout layoutNoResults;
    private TextView textViewSearchQuery;

    private ProductDao productDao;
    private List<Product> searchResults;
    private String searchQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        productDao = new ProductDao(this);

        searchQuery = getIntent().getStringExtra("query");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Search Results");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        initViews();
        textViewSearchQuery.setText("Results for: \"" + searchQuery + "\"");
        loadSearchResults();
        backButton();
    }

    private void backButton() {
        ImageButton productBackIcon = findViewById(R.id.resultBackIcon);
        productBackIcon.setOnClickListener(v -> finish()); // Closes the current activity
    }

    private void initViews() {
        recyclerViewSearchResults = findViewById(R.id.recyclerViewSearchResults);
        layoutNoResults = findViewById(R.id.layoutNoResults);
        textViewSearchQuery = findViewById(R.id.textViewSearchQuery);
    }

    private void loadSearchResults() {
        searchResults = productDao.searchProducts(searchQuery);

        if (searchResults.isEmpty()) {
            recyclerViewSearchResults.setVisibility(View.GONE);
            layoutNoResults.setVisibility(View.VISIBLE);
        } else {
            recyclerViewSearchResults.setVisibility(View.VISIBLE);
            layoutNoResults.setVisibility(View.GONE);
            GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
            recyclerViewSearchResults.setLayoutManager(layoutManager);
            ProductAdapter adapter = new ProductAdapter(searchResults);
            adapter.setOnItemClickListener(product -> {
                Intent intent = new Intent(this, ProductDetailsActivity.class);
                intent.putExtra("product_id", product.getId());
                startActivity(intent);
            });
            recyclerViewSearchResults.setAdapter(adapter);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Simply finish this activity instead of creating a new intent
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Simply finish this activity
        finish();
    }
}