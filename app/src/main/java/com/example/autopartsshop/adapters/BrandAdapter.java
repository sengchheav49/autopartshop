package com.example.autopartsshop.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.autopartsshop.R;
import com.example.autopartsshop.models.Brand;
import com.example.autopartsshop.utils.ImageUtils;

import java.util.List;

public class BrandAdapter extends RecyclerView.Adapter<BrandAdapter.BrandViewHolder> {

    private Context context;
    private List<Brand> brands;
    private OnBrandClickListener listener;
    private OnBrandLongClickListener longClickListener;
    private boolean isEditable = false;

    // Interface for click events
    public interface OnBrandClickListener {
        void onBrandClick(Brand brand, int position);
    }
    
    // Interface for long click events (editing)
    public interface OnBrandLongClickListener {
        boolean onBrandLongClick(Brand brand, int position);
    }

    public BrandAdapter(Context context, List<Brand> brands, OnBrandClickListener listener) {
        this.context = context;
        this.brands = brands;
        this.listener = listener;
    }
    
    public BrandAdapter(Context context, List<Brand> brands, OnBrandClickListener listener, 
                       OnBrandLongClickListener longClickListener) {
        this.context = context;
        this.brands = brands;
        this.listener = listener;
        this.longClickListener = longClickListener;
        this.isEditable = longClickListener != null;
    }

    @NonNull
    @Override
    public BrandViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_brand, parent, false);
        return new BrandViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BrandViewHolder holder, int position) {
        Brand brand = brands.get(position);
        
        // Set brand name
        holder.brandName.setText(brand.getName());
        
        // Set brand logo from BLOB data
        if (brand.getLogoData() != null) {
            ImageUtils.setImageFromByteArray(context, holder.brandLogo, brand.getLogoData());
        } else {
            // Set a default logo if no logo data is available
            holder.brandLogo.setImageResource(R.drawable.ic_default_brand);
        }
        
        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBrandClick(brand, position);
            }
        });
        
        // Set long click listener for editing if available
        if (isEditable) {
            holder.itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    return longClickListener.onBrandLongClick(brand, position);
                }
                return false;
            });
            
            // Show an edit indicator if editable (optional)
            if (brand.getId() > 0) { // Only add indicator for real brands, not "All Brands"
                holder.itemView.setBackgroundResource(R.drawable.selectable_item_background);
            }
        }
    }

    @Override
    public int getItemCount() {
        return brands != null ? brands.size() : 0;
    }

    public void updateData(List<Brand> newBrands) {
        this.brands = newBrands;
        notifyDataSetChanged();
    }
    
    public void setEditable(boolean editable) {
        this.isEditable = editable;
        notifyDataSetChanged();
    }

    public static class BrandViewHolder extends RecyclerView.ViewHolder {
        ImageView brandLogo;
        TextView brandName;

        public BrandViewHolder(@NonNull View itemView) {
            super(itemView);
            brandLogo = itemView.findViewById(R.id.brand_logo);
            brandName = itemView.findViewById(R.id.brand_name);
        }
    }
} 