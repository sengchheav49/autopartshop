package com.example.autopartsshop.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.autopartsshop.R;

import java.util.List;

public class ProductImageAdapter extends RecyclerView.Adapter<ProductImageAdapter.ProductImageViewHolder> {
    private List<Bitmap> imageList;

    public ProductImageAdapter(List<Bitmap> imageList) {
        this.imageList = imageList;
    }

    @Override
    public ProductImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new ProductImageViewHolder(view);
    }

     @Override
    public void onBindViewHolder(ProductImageViewHolder holder, int position) {
        Bitmap bitmap = imageList.get(position);
        holder.productImageView.setImageBitmap(bitmap);  // Set the bitmap image to ImageView
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    // ViewHolder class to hold the ImageView
    class ProductImageViewHolder extends RecyclerView.ViewHolder {
        ImageView productImageView;

        public ProductImageViewHolder(View itemView) {
            super(itemView);
            productImageView = itemView.findViewById(R.id.imageViewBrandLogo);  // Assuming the ImageView ID is product_image
        }
    }
}
