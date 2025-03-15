package com.example.autopartsshop.adapters;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.autopartsshop.R;
import com.example.autopartsshop.database.CartDao;
import com.example.autopartsshop.models.CartItem;
import com.example.autopartsshop.models.Product;
import com.example.autopartsshop.utils.Constants;
import com.example.autopartsshop.utils.ImageUtils;
import com.example.autopartsshop.utils.SharedPrefManager;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final List<Product> productList;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(Product product);
    }

    public ProductAdapter(List<Product> productList) {
        this.productList = productList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.bind(product);

        // Ensure button width consistency
        ViewGroup.LayoutParams params = holder.buttonAddToCart.getLayoutParams();
        holder.buttonAddToCart.setLayoutParams(params);

        // Shorten button text
        holder.buttonAddToCart.setText("Add Cart");

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(product);
            }
        });
    }

    // Convert dp to pixels
    private int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageViewProduct;
        private final TextView textViewProductName;
        private final TextView textViewProductCategory;
        private final TextView textViewProductPrice;
        private final Button buttonAddToCart;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProduct = itemView.findViewById(R.id.imageViewProduct);
            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            textViewProductCategory = itemView.findViewById(R.id.textViewProductCategory);
            textViewProductPrice = itemView.findViewById(R.id.textViewProductPrice);
            buttonAddToCart = itemView.findViewById(R.id.buttonAddToCart);
        }
        public void bind(Product product) {
            textViewProductName.setText(product.getName());
            textViewProductCategory.setText(product.getCategory());
            textViewProductPrice.setText(product.getFormattedPrice());

            // Use the new ImageUtils method to load and fit the image
            byte[] imageData = product.getImageData();
            ImageUtils.loadImageWithFit(
                itemView.getContext(),
                imageViewProduct,
                imageData != null && imageData.length > 0 ? imageData : R.drawable.ic_add,
                R.drawable.ic_add, // Placeholder
                R.drawable.ic_add  // Error image
            );

            buttonAddToCart.setOnClickListener(v -> {
                SharedPrefManager sharedPrefManager = SharedPrefManager.getInstance(itemView.getContext());

                if (sharedPrefManager.isLoggedIn()) {
                    CartDao cartDao = new CartDao(itemView.getContext());
                    int userId = sharedPrefManager.getUser().getId();

                    CartItem cartItem = new CartItem(userId, product.getId(), Constants.DEFAULT_QUANTITY);
                    long insertedRowId = cartDao.addToCart(cartItem);

                    if (insertedRowId > 0) {
                        Toast.makeText(itemView.getContext(), "Added to cart", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(itemView.getContext(), "Failed to add item", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(itemView.getContext(), "Please login to add items to cart", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }
}
