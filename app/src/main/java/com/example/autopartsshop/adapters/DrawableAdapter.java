package com.example.autopartsshop.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.autopartsshop.R;

import java.util.List;

public class DrawableAdapter extends RecyclerView.Adapter<DrawableAdapter.DrawableViewHolder> {

    private static final String TAG = "DrawableAdapter";
    private Context context;
    private List<Integer> drawableIds;
    private OnDrawableClickListener listener;

    // Interface for click events
    public interface OnDrawableClickListener {
        void onDrawableClick(int drawableResId);
    }

    public DrawableAdapter(Context context, List<Integer> drawableIds, OnDrawableClickListener listener) {
        this.context = context;
        this.drawableIds = drawableIds;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DrawableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_drawable, parent, false);
        return new DrawableViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DrawableViewHolder holder, int position) {
        try {
            int drawableId = drawableIds.get(position);
            
            // Set the drawable resource with error handling
            try {
                holder.drawableImage.setImageResource(drawableId);
            } catch (Resources.NotFoundException e) {
                Log.e(TAG, "Resource not found: " + drawableId, e);
                // Set a default image if the resource is not found
                holder.drawableImage.setImageResource(R.drawable.ic_default_brand);
            } catch (Exception e) {
                Log.e(TAG, "Error setting image resource: " + e.getMessage(), e);
                holder.drawableImage.setImageResource(R.drawable.ic_default_brand);
            }
            
            // Set click listener
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    try {
                        listener.onDrawableClick(drawableId);
                    } catch (Exception e) {
                        Log.e(TAG, "Error on drawable click: " + e.getMessage(), e);
                    }
                }
            });
        } catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "Index out of bounds at position: " + position, e);
            holder.drawableImage.setImageResource(R.drawable.ic_default_brand);
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in onBindViewHolder: " + e.getMessage(), e);
            holder.drawableImage.setImageResource(R.drawable.ic_default_brand);
        }
    }

    @Override
    public int getItemCount() {
        return drawableIds != null ? drawableIds.size() : 0;
    }

    public static class DrawableViewHolder extends RecyclerView.ViewHolder {
        ImageView drawableImage;

        public DrawableViewHolder(@NonNull View itemView) {
            super(itemView);
            drawableImage = itemView.findViewById(R.id.drawable_image);
        }
    }
} 