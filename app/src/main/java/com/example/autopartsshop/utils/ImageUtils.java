package com.example.autopartsshop.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Base64;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.autopartsshop.R;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;

public class ImageUtils {

    // Convert bitmap to base64 string
    public static String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    // Convert base64 string to bitmap
    public static Bitmap base64ToBitmap(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            return null;
        }

        byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    public static void scaleImage(ImageView view) {
        try {
            // Get the drawable from ImageView
            Drawable drawing = view.getDrawable();
            if (drawing == null) {
                throw new NoSuchElementException("No drawable found on the given ImageView.");
            }

            Bitmap bitmap = ((BitmapDrawable) drawing).getBitmap();

            // Get current dimensions and define target size
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int bounding = dpToPx(view, 250); // Convert 250dp to pixels

            // Calculate scale factor
            float xScale = ((float) bounding) / width;
            float yScale = ((float) bounding) / height;
            float scale = Math.min(xScale, yScale); // Maintain aspect ratio

            // Create a scaled bitmap
            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);
            Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);

            // Apply scaled bitmap to ImageView
            view.setImageDrawable(new BitmapDrawable(view.getResources(), scaledBitmap));

            // Adjust ImageView size to match the scaled image
            ViewGroup.LayoutParams params = view.getLayoutParams();
            params.width = scaledBitmap.getWidth();
            params.height = scaledBitmap.getHeight();
            view.setLayoutParams(params);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Helper method to convert dp to pixels
    private static int dpToPx(ImageView view, int dp) {
        float density = view.getContext().getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
    
    // Convert Uri to byte array
    public static byte[] convertImageUriToByteArray(Context context, Uri uri) {
        try {
            // Get the InputStream from the Uri
            InputStream inputStream = context.getContentResolver().openInputStream(uri);

            // Convert InputStream to byte array
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, length);
            }

            // Return the byte array
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;  // Return null if there's an error
        }
    }

    // Get bitmap from Uri
    public static Bitmap getBitmapFromUri(Context context, Uri uri) throws FileNotFoundException {
        InputStream input = context.getContentResolver().openInputStream(uri);
        return BitmapFactory.decodeStream(input);
    }
    
    // Set image from resource name (drawable)
    public static void setImageFromResourceName(Context context, ImageView imageView, String imageName) {
        int resId = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
        if (resId != 0) {
            imageView.setImageResource(resId);
        } else {
            // Use a default placeholder image if the resource is not found
            imageView.setImageResource(R.drawable.ic_add_product);
        }
    }

    // New method to set image from byte array
    // Set image from byte array (for images like the ones downloaded from a server)
    public static void setImageFromByteArray(Context context, ImageView imageView, byte[] imageData) {
        if (imageData != null) {
            Glide.with(context)
                    .load(imageData)  // Load image from byte array
                    .into(imageView);
        } else {
            // Use a default placeholder image if imageData is null
            imageView.setImageResource(R.drawable.ic_add);
        }
    }

    public static int getResourceIdByName(Context context, String name) {
        if (name == null || name.isEmpty()) {
            return -1;
        }

        return context.getResources().getIdentifier(
                name, "drawable", context.getPackageName());
    }
    
    // Inside ImageUtils.java
    // Generate a unique image name based on timestamp
    public static String generateImageName() {
        long timestamp = System.currentTimeMillis();
        return "image_" + timestamp + ".jpg";
    }

    // Resize bitmap to a specified width and height
    public static Bitmap resizeBitmap(Bitmap bitmap, int width, int height) {
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    // ========== NEW BLOB HANDLING METHODS ==========
    
    /**
     * Convert any file URI to byte array for blob storage
     * @param context The application context
     * @param uri The URI of the file to convert
     * @return byte array representation of the file
     */
    public static byte[] uriToBlob(Context context, Uri uri) {
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            if (inputStream == null) {
                return null;
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096]; // Use larger buffer for efficiency
            int bytesRead;
            
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            
            return outputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Compress a bitmap to a byte array with specified quality
     * @param bitmap The bitmap to compress
     * @param format The compression format (JPEG, PNG, WEBP)
     * @param quality Compression quality (0-100)
     * @return Compressed byte array
     */
    public static byte[] bitmapToBlob(Bitmap bitmap, Bitmap.CompressFormat format, int quality) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(format, quality, outputStream);
        return outputStream.toByteArray();
    }
    
    /**
     * Convert byte array to base64 string for network transmission or storage
     * @param blob The byte array to convert
     * @return Base64 encoded string
     */
    public static String blobToBase64(byte[] blob) {
        if (blob == null) {
            return null;
        }
        return Base64.encodeToString(blob, Base64.DEFAULT);
    }
    
    /**
     * Convert base64 string back to byte array
     * @param base64String The base64 encoded string
     * @return Decoded byte array
     */
    public static byte[] base64ToBlob(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            return null;
        }
        return Base64.decode(base64String, Base64.DEFAULT);
    }
    
    /**
     * Efficiently retrieve a scaled-down version of an image blob
     * Useful for thumbnails or previews
     * @param imageBlob The full image blob
     * @param maxWidth Maximum width of the resulting bitmap
     * @param maxHeight Maximum height of the resulting bitmap
     * @return Scaled-down bitmap
     */
    public static Bitmap getScaledBitmapFromBlob(byte[] imageBlob, int maxWidth, int maxHeight) {
        if (imageBlob == null) {
            return null;
        }
        
        // First decode with inJustDecodeBounds=true to check dimensions
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(imageBlob, 0, imageBlob.length, options);
        
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight);
        
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(imageBlob, 0, imageBlob.length, options);
    }
    
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        
        return inSampleSize;
    }

    /**
     * Consistently load and fit images from various sources (byte arrays, URIs, resource IDs)
     * This method provides a unified way to display images that maintains aspect ratio without distortion
     * 
     * @param context Application context
     * @param imageView The ImageView to load the image into
     * @param imageSource The image source (byte[], Uri, Integer resource ID, or String path/URL)
     * @param placeholderResId Resource ID for placeholder image (optional, use 0 for none)
     * @param errorResId Resource ID for error image (optional, use 0 for none)
     */
    public static void loadImageWithFit(Context context, ImageView imageView, Object imageSource, 
                                     int placeholderResId, int errorResId) {
        if (context == null || imageView == null) {
            return;
        }
        
        // Configure request options with centerCrop for all images
        RequestOptions options = new RequestOptions()
            .centerCrop() // Use centerCrop for all images to maintain aspect ratio while filling the view
            .diskCacheStrategy(DiskCacheStrategy.ALL); // Cache images for better performance
            
        // Set placeholder if provided    
        if (placeholderResId != 0) {
            options = options.placeholder(placeholderResId);
        }
        
        // Set error image if provided
        if (errorResId != 0) {
            options = options.error(errorResId);
        }
        
        // Load image from the appropriate source
        if (imageSource instanceof byte[]) {
            // Load from byte array (BLOB)
            Glide.with(context)
                .load((byte[]) imageSource)
                .apply(options)
                .into(imageView);
        } else if (imageSource instanceof Uri) {
            // Load from URI
            Glide.with(context)
                .load((Uri) imageSource)
                .apply(options)
                .into(imageView);
        } else if (imageSource instanceof Integer) {
            // Load from resource ID
            Glide.with(context)
                .load((Integer) imageSource)
                .apply(options)
                .into(imageView);
        } else if (imageSource instanceof String) {
            // Load from URL or file path
            Glide.with(context)
                .load((String) imageSource)
                .apply(options)
                .into(imageView);
        } else if (imageSource instanceof Bitmap) {
            // Load from Bitmap
            Glide.with(context)
                .load((Bitmap) imageSource)
                .apply(options)
                .into(imageView);
        } else {
            // If source is invalid, load error drawable or clear
            if (errorResId != 0) {
                imageView.setImageResource(errorResId);
            }
        }
    }
}
