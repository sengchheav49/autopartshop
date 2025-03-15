package com.example.autopartsshop.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.example.autopartsshop.R;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

public class DrawableUtils {
    private static final String TAG = "DrawableUtils";
    
    /**
     * Get a list of car brand drawable resource IDs
     * @return List of car brand drawable resource IDs
     */
    public static List<Integer> getCarBrandDrawables() {
        // Only use drawable resources that actually exist in the project
        return Arrays.asList(
            R.drawable.bmw_m5_img,
            R.drawable.bugati,
            R.drawable.ferrari,
            R.drawable.ferrari1,
            R.drawable.ford,



            R.drawable.lafarrari,
            R.drawable.lamborghini,
            R.drawable.lamborghini1,

            R.drawable.landrover,
            R.drawable.landrover1,
            R.drawable.landrover2,

            R.drawable.matin,
            R.drawable.mazda,
            R.drawable.mclaren,
            R.drawable.mercedes,
            R.drawable.mercedes2,
            R.drawable.mercedes3,

            R.drawable.nissan,
            R.drawable.pagani,
            R.drawable.porch,
            R.drawable.porsche,

            R.drawable.rolls_royce,
            R.drawable.toyata,
            R.drawable.volkswagen
        );
    }
    
    /**
     * Convert a drawable resource to a byte array
     * @param context The context
     * @param drawableId The drawable resource ID
     * @return The drawable as a byte array
     */
    public static byte[] drawableToByteArray(Context context, int drawableId) {
        try {
            Drawable drawable = ContextCompat.getDrawable(context, drawableId);
            return drawableToByteArray(drawable);
        } catch (Exception e) {
            Log.e(TAG, "Error converting drawable to byte array", e);
            return null;
        }
    }
    
    /**
     * Convert a drawable to a byte array
     * @param drawable The drawable
     * @return The drawable as a byte array
     */
    public static byte[] drawableToByteArray(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        
        Bitmap bitmap = null;
        ByteArrayOutputStream stream = null;
        byte[] byteArray = null;
        
        try {
            if (drawable instanceof BitmapDrawable) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                bitmap = bitmapDrawable.getBitmap();
                // Don't recycle this bitmap as it may be cached and reused elsewhere
            } else {
                if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
                    bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
                } else {
                    bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), 
                                                drawable.getIntrinsicHeight(), 
                                                Bitmap.Config.ARGB_8888);
                }
                
                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);
                // This bitmap is created by us, but we still shouldn't recycle it yet
            }
            
            stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byteArray = stream.toByteArray();
            
            // We no longer recycle the bitmap here, as it may be used elsewhere
            // bitmap.recycle(); - REMOVED THIS LINE
            
            return byteArray;
        } catch (Exception e) {
            Log.e(TAG, "Error in drawableToByteArray", e);
            return null;
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error closing stream", e);
            }
            // We don't recycle the bitmap here either
        }
    }
    
    /**
     * Show a dialog to select a drawable
     * @param context The context
     * @param listener Callback for when a drawable is selected
     */
    public static void showDrawableSelector(Context context, OnDrawableSelectedListener listener) {
        showDrawableSelector(context, getCarBrandDrawables(), listener);
    }
    
    /**
     * Show a dialog to select a drawable
     * @param context The context
     * @param drawableIds List of drawable resource IDs to display
     * @param listener Callback for when a drawable is selected
     */
    public static void showDrawableSelector(Context context, List<Integer> drawableIds, 
                                          OnDrawableSelectedListener listener) {
        try {
            DrawableSelectorDialog dialog = new DrawableSelectorDialog(context, drawableIds, listener);
            dialog.show();
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Resource not found: " + e.getMessage());
            // Provide feedback that there was an error
            if (listener != null) {
                listener.onDrawableSelected(R.drawable.ic_default_brand, 
                    drawableToByteArray(context, R.drawable.ic_default_brand));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing drawable selector: " + e.getMessage(), e);
        }
    }
    
    /**
     * Interface for drawable selection callback
     */
    public interface OnDrawableSelectedListener {
        void onDrawableSelected(int drawableResId, byte[] drawableData);
    }
} 