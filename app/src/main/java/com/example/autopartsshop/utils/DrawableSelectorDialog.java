package com.example.autopartsshop.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.autopartsshop.R;
import com.example.autopartsshop.adapters.DrawableAdapter;

import java.util.ArrayList;
import java.util.List;

public class DrawableSelectorDialog implements DrawableAdapter.OnDrawableClickListener {

    private static final String TAG = "DrawableSelectorDialog";
    private Dialog dialog;
    private Context context;
    private List<Integer> drawableIds;
    private DrawableUtils.OnDrawableSelectedListener listener;

    public DrawableSelectorDialog(Context context, List<Integer> drawableIds, 
                                 DrawableUtils.OnDrawableSelectedListener listener) {
        this.context = context;
        this.drawableIds = validateDrawableIds(context, drawableIds);
        this.listener = listener;
        
        createDialog();
    }

    /**
     * Validate drawable IDs to ensure they exist
     * @param context The context
     * @param drawableIds The list of drawable IDs to validate
     * @return A list of valid drawable IDs
     */
    private List<Integer> validateDrawableIds(Context context, List<Integer> drawableIds) {
        List<Integer> validIds = new ArrayList<>();
        
        // Always include default icon as a fallback
        validIds.add(R.drawable.ic_default_brand);
        
        // Check if each drawable ID is valid
        if (drawableIds != null) {
            for (Integer id : drawableIds) {
                try {
                    // Try to get the drawable to check if it exists
                    if (context.getResources().getDrawable(id) != null) {
                        validIds.add(id);
                    }
                } catch (Resources.NotFoundException e) {
                    Log.w(TAG, "Drawable resource not found: " + id);
                } catch (Exception e) {
                    Log.e(TAG, "Error validating drawable: " + e.getMessage());
                }
            }
        }
        
        return validIds;
    }

    private void createDialog() {
        try {
            dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_drawable_selector);
            dialog.setCancelable(true);
            
            // Set up RecyclerView
            RecyclerView recyclerView = dialog.findViewById(R.id.drawable_recycler_view);
            recyclerView.setLayoutManager(new GridLayoutManager(context, 3));
            
            // Set up adapter
            DrawableAdapter adapter = new DrawableAdapter(context, drawableIds, this);
            recyclerView.setAdapter(adapter);
            
            // Set up cancel button
            Button cancelButton = dialog.findViewById(R.id.btn_cancel);
            cancelButton.setOnClickListener(v -> dialog.dismiss());
        } catch (Exception e) {
            Log.e(TAG, "Error creating dialog: " + e.getMessage());
            Toast.makeText(context, "Error creating selector", Toast.LENGTH_SHORT).show();
        }
    }

    public void show() {
        try {
            if (dialog != null) {
                dialog.show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing dialog: " + e.getMessage());
            Toast.makeText(context, "Could not show selector", Toast.LENGTH_SHORT).show();
            
            // Notify the listener with a default drawable
            if (listener != null) {
                listener.onDrawableSelected(R.drawable.ic_default_brand, 
                    DrawableUtils.drawableToByteArray(context, R.drawable.ic_default_brand));
            }
        }
    }

    public void dismiss() {
        try {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error dismissing dialog: " + e.getMessage());
        }
    }

    @Override
    public void onDrawableClick(int drawableResId) {
        try {
            if (listener != null) {
                byte[] drawableData = DrawableUtils.drawableToByteArray(context, drawableResId);
                listener.onDrawableSelected(drawableResId, drawableData);
            }
            dismiss();
        } catch (Exception e) {
            Log.e(TAG, "Error handling drawable click: " + e.getMessage());
            Toast.makeText(context, "Error selecting image", Toast.LENGTH_SHORT).show();
            dismiss();
        }
    }
} 