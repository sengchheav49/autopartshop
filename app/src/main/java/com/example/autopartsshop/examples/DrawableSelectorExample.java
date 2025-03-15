package com.example.autopartsshop.examples;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.autopartsshop.R;
import com.example.autopartsshop.database.BrandDao;
import com.example.autopartsshop.database.ProductDao;
import com.example.autopartsshop.models.Brand;
import com.example.autopartsshop.utils.DrawableUtils;
import com.example.autopartsshop.utils.ImageUtils;

/**
 * Example class showing how to use the drawable selector
 * This is a working example that can be used to test the drawable selector functionality
 */
public class DrawableSelectorExample extends AppCompatActivity {

    private ImageView selectedLogoImageView;
    private Button selectLogoButton;
    private int selectedBrandId = -1;
    private byte[] selectedLogoData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawable_selector_example);

        // Initialize views
        selectedLogoImageView = findViewById(R.id.selected_logo_image);
        selectLogoButton = findViewById(R.id.select_logo_button);

        // Set up button click listener
        selectLogoButton.setOnClickListener(v -> showDrawableSelector());
        
        // Optional: Set a default brand to work with
        // You can set this to an actual brand ID from your database
        // Or you can add a spinner to select brands
        selectedBrandId = 1; // Assuming brand with ID 1 exists
    }

    private void showDrawableSelector() {
        // Show the drawable selector dialog
        DrawableUtils.showDrawableSelector(this, (drawableResId, drawableData) -> {
            // Handle the selected drawable
            selectedLogoData = drawableData;
            
            // Display the selected drawable in the ImageView
            selectedLogoImageView.setImageResource(drawableResId);
            
            // Example: Save the logo to a brand
            if (selectedBrandId != -1) {
                saveBrandLogo(selectedBrandId, drawableData);
            }
        });
    }

    private void saveBrandLogo(int brandId, byte[] logoData) {
        // Example of saving the logo to a brand
        BrandDao brandDao = new BrandDao(this);
        Brand brand = brandDao.getBrandById(brandId);
        
        if (brand != null) {
            brand.setLogoData(logoData);
            int result = brandDao.updateBrand(brand, null);
            
            if (result > 0) {
                Toast.makeText(this, "Brand logo updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to update brand logo", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Brand not found with ID: " + brandId, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Alternative method that uses the ProductDao helper method
     */
    private void saveBrandLogoUsingProductDao(int brandId) {
        // This example uses the helper method in ProductDao
        ProductDao productDao = new ProductDao(this);
        
        // Convert drawable to Uri (not actually needed for our implementation)
        // This is just to show an alternative approach
        // In this case, null means we'll use the byte[] directly
        productDao.addBrandLogo(brandId, null);
        
        Toast.makeText(this, "Brand logo updated", Toast.LENGTH_SHORT).show();
    }
} 