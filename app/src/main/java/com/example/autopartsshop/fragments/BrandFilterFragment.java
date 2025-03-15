package com.example.autopartsshop.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.autopartsshop.R;
import com.example.autopartsshop.adapters.BrandAdapter;
import com.example.autopartsshop.database.BrandDao;
import com.example.autopartsshop.models.Brand;
import com.example.autopartsshop.utils.DrawableUtils;

import java.util.ArrayList;
import java.util.List;

public class BrandFilterFragment extends Fragment implements 
        BrandAdapter.OnBrandClickListener, 
        BrandAdapter.OnBrandLongClickListener {

    private RecyclerView brandRecyclerView;
    private BrandAdapter brandAdapter;
    private BrandDao brandDao;
    private OnBrandSelectedListener listener;
    private boolean isEditable = false; // Set to true to enable logo editing

    // Interface for communication with parent activity
    public interface OnBrandSelectedListener {
        void onBrandSelected(Brand brand);
        void onAllBrandsSelected();
    }

    public BrandFilterFragment() {
        // Required empty public constructor
    }

    public static BrandFilterFragment newInstance() {
        return new BrandFilterFragment();
    }

    public static BrandFilterFragment newInstanceEditable() {
        BrandFilterFragment fragment = new BrandFilterFragment();
        fragment.isEditable = true;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.brand_filter_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize BrandDao
        brandDao = new BrandDao(requireContext());

        // Initialize RecyclerView
        brandRecyclerView = view.findViewById(R.id.brand_recycler_view);

        // Create and set adapter
        List<Brand> brands = new ArrayList<>();
        
        // Use the right constructor based on editability
        if (isEditable) {
            brandAdapter = new BrandAdapter(requireContext(), brands, this, this);
        } else {
            brandAdapter = new BrandAdapter(requireContext(), brands, this);
        }
        
        brandRecyclerView.setAdapter(brandAdapter);

        // Get brands from database and update adapter
        loadBrands();
    }

    private void loadBrands() {
        // Get active brands from database
        List<Brand> dbBrands = brandDao.getActiveBrands();
        
        // Create a new list with "All Brands" at the beginning
        List<Brand> displayBrands = new ArrayList<>();
        
        // Add "All Brands" item
        Brand allBrands = new Brand();
        allBrands.setId(-1);
        allBrands.setName("All Brands");
        displayBrands.add(allBrands);
        
        // Add all other brands
        displayBrands.addAll(dbBrands);
        
        // Update adapter
        brandAdapter.updateData(displayBrands);
    }

    @Override
    public void onBrandClick(Brand brand, int position) {
        if (listener != null) {
            if (brand.getId() == -1) {
                // "All Brands" selected
                listener.onAllBrandsSelected();
            } else {
                // Specific brand selected
                listener.onBrandSelected(brand);
            }
        }
    }

    @Override
    public boolean onBrandLongClick(Brand brand, int position) {
        // Only allow editing logos for actual brands (not "All Brands")
        if (isEditable && brand.getId() > 0) {
            showDrawableSelectorForBrand(brand);
            return true; // Event handled
        }
        return false; // Not handled
    }

    /**
     * Show drawable selector dialog to pick a logo for a brand
     * @param brand The brand to update
     */
    private void showDrawableSelectorForBrand(Brand brand) {
        DrawableUtils.showDrawableSelector(requireContext(), (drawableResId, drawableData) -> {
            // Update brand with new logo
            brand.setLogoData(drawableData);
            int result = brandDao.updateBrand(brand, null);
            
            if (result > 0) {
                Toast.makeText(requireContext(), 
                    "Logo updated for " + brand.getName(), 
                    Toast.LENGTH_SHORT).show();
                
                // Reload brands to show the updated logo
                loadBrands();
            } else {
                Toast.makeText(requireContext(), 
                    "Failed to update logo for " + brand.getName(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setOnBrandSelectedListener(OnBrandSelectedListener listener) {
        this.listener = listener;
    }
    
    /**
     * Set whether this fragment should allow editing brand logos
     * @param editable True to enable editing, false otherwise
     */
    public void setEditable(boolean editable) {
        this.isEditable = editable;
        if (brandAdapter != null) {
            brandAdapter.setEditable(editable);
        }
    }
} 