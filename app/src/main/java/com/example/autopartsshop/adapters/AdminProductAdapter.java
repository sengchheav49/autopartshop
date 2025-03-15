package com.example.autopartsshop.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.autopartsshop.R;
import com.example.autopartsshop.models.Product;
import com.example.autopartsshop.utils.ImageUtils;

import java.util.List;

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.AdminProductViewHolder> {

    private final List<Product> productList;
    private OnItemClickListener onItemClickListener;
    private OnMenuClickListener onMenuClickListener;

    public interface OnItemClickListener {
        void onItemClick(Product product);
    }

    public interface OnMenuClickListener {
        void onMenuClick(Product product, View view);
    }

    public AdminProductAdapter(List<Product> productList) {
        this.productList = productList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setOnMenuClickListener(OnMenuClickListener listener) {
        this.onMenuClickListener = listener;
    }

    @NonNull
    @Override
    public AdminProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_product, parent, false);
        return new AdminProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.bind(product);

        // Click listeners
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(product);
            }
        });

        holder.menuButtonAdminProduct.setOnClickListener(v -> {
            if (onMenuClickListener != null) {
                onMenuClickListener.onMenuClick(product, v);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class AdminProductViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageViewAdminProduct;
        private final TextView textViewAdminProductName;
        private final TextView textViewAdminProductCategory;
        private final TextView textViewAdminProductPrice;
        private final TextView textViewAdminProductStock;
        private final ImageButton menuButtonAdminProduct;

        public AdminProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewAdminProduct = itemView.findViewById(R.id.imageViewAdminProduct);
            textViewAdminProductName = itemView.findViewById(R.id.textViewAdminProductName);
            textViewAdminProductCategory = itemView.findViewById(R.id.textViewAdminProductCategory);
            textViewAdminProductPrice = itemView.findViewById(R.id.textViewAdminProductPrice);
            textViewAdminProductStock = itemView.findViewById(R.id.textViewAdminProductStock);
            menuButtonAdminProduct = itemView.findViewById(R.id.menuButtonAdminProduct);
        }

        public void bind(Product product) {
            // Check for null or empty fields to avoid crashing the app
            textViewAdminProductName.setText(product.getName() != null ? product.getName() : "No Name");
            textViewAdminProductCategory.setText(product.getCategory() != null ? product.getCategory() : "No Category");
            textViewAdminProductPrice.setText(product.getPrice() >= 0 ? String.format("$%.2f", product.getPrice()) : "$0.00");

            // Set stock text (handle case where stock is less than 0)
            String stockText = (product.getStock() >= 0 ? product.getStock() : 0) + " in stock";
            textViewAdminProductStock.setText(stockText);

            // Use the new ImageUtils method to load and fit the image
            byte[] imageData = product.getImageData();
            ImageUtils.loadImageWithFit(
                itemView.getContext(),
                imageViewAdminProduct,
                imageData != null && imageData.length > 0 ? imageData : R.drawable.ic_add,
                R.drawable.ic_add, // Placeholder
                R.drawable.ic_add  // Error image
            );
        }
    }
}
