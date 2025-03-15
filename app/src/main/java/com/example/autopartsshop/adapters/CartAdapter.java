package com.example.autopartsshop.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.autopartsshop.R;
import com.example.autopartsshop.models.CartItem;
import com.example.autopartsshop.utils.Constants;
import com.example.autopartsshop.utils.ImageUtils;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final List<CartItem> cartItemList;
    private OnQuantityChangeListener onQuantityChangeListener;
    private OnDeleteClickListener onDeleteClickListener;

    public interface OnQuantityChangeListener {
        void onQuantityChanged(CartItem cartItem, int newQuantity);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(CartItem cartItem);
    }

    public CartAdapter(List<CartItem> cartItemList) {
        this.cartItemList = cartItemList;
    }

    public void setOnQuantityChangeListener(OnQuantityChangeListener listener) {
        this.onQuantityChangeListener = listener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.onDeleteClickListener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem cartItem = cartItemList.get(position);
        holder.bind(cartItem);

        // Decrease quantity
        holder.buttonDecreaseCartQuantity.setOnClickListener(v -> {
            int currentQuantity = cartItem.getQuantity();
            if (currentQuantity > 1) {
                int newQuantity = currentQuantity - 1;
                cartItem.setQuantity(newQuantity);
                holder.textViewCartItemQuantity.setText(String.valueOf(newQuantity));
                holder.textViewCartItemSubtotal.setText(cartItem.getFormattedSubtotal());

                if (onQuantityChangeListener != null) {
                    onQuantityChangeListener.onQuantityChanged(cartItem, newQuantity);
                }
            }
        });

        // Increase quantity
        holder.buttonIncreaseCartQuantity.setOnClickListener(v -> {
            int currentQuantity = cartItem.getQuantity();
            if (currentQuantity < Math.min(cartItem.getProduct().getStock(), Constants.MAX_QUANTITY)) {
                int newQuantity = currentQuantity + 1;
                cartItem.setQuantity(newQuantity);
                holder.textViewCartItemQuantity.setText(String.valueOf(newQuantity));
                holder.textViewCartItemSubtotal.setText(cartItem.getFormattedSubtotal());

                if (onQuantityChangeListener != null) {
                    onQuantityChangeListener.onQuantityChanged(cartItem, newQuantity);
                }
            } else {
                Toast.makeText(v.getContext(), "Maximum quantity reached", Toast.LENGTH_SHORT).show();
            }
        });

        // Delete item
        holder.imageButtonDelete.setOnClickListener(v -> {
            if (onDeleteClickListener != null) {
                onDeleteClickListener.onDeleteClick(cartItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageViewCartItemProduct;
        private final TextView textViewCartItemName;
        private final TextView textViewCartItemPrice;
        private final TextView textViewCartItemQuantity;
        private final TextView textViewCartItemSubtotal;
        private final ImageButton buttonDecreaseCartQuantity;
        private final ImageButton buttonIncreaseCartQuantity;
        private final ImageButton imageButtonDelete;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewCartItemProduct = itemView.findViewById(R.id.imageViewCartItemProduct);
            textViewCartItemName = itemView.findViewById(R.id.textViewCartItemName);
            textViewCartItemPrice = itemView.findViewById(R.id.textViewCartItemPrice);
            textViewCartItemQuantity = itemView.findViewById(R.id.textViewCartItemQuantity);
            textViewCartItemSubtotal = itemView.findViewById(R.id.textViewCartItemSubtotal);
            buttonDecreaseCartQuantity = itemView.findViewById(R.id.buttonDecreaseCartQuantity);
            buttonIncreaseCartQuantity = itemView.findViewById(R.id.buttonIncreaseCartQuantity);
            imageButtonDelete = itemView.findViewById(R.id.imageButtonDelete);
        }

        public void bind(CartItem cartItem) {
            // Set product details
            textViewCartItemName.setText(cartItem.getProduct().getName());
            textViewCartItemPrice.setText(cartItem.getProduct().getFormattedPrice());
            textViewCartItemQuantity.setText(String.valueOf(cartItem.getQuantity()));
            textViewCartItemSubtotal.setText(cartItem.getFormattedSubtotal());

            // Use the new ImageUtils method for consistent image display
            byte[] imageData = cartItem.getProduct().getImageData();
            ImageUtils.loadImageWithFit(
                itemView.getContext(),
                imageViewCartItemProduct,
                imageData != null && imageData.length > 0 ? imageData : R.drawable.ic_cart,
                R.drawable.ic_cart, // Placeholder
                R.drawable.ic_cart  // Error image
            );
        }
    }
}
