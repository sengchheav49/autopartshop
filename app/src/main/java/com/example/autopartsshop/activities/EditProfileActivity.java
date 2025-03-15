package com.example.autopartsshop.activities;

import static com.example.autopartsshop.utils.ImageUtils.resizeBitmap;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.autopartsshop.R;
import com.example.autopartsshop.database.UserDao;
import com.example.autopartsshop.models.User;
import com.example.autopartsshop.utils.DrawableUtils;
import com.example.autopartsshop.utils.ImageUtils;
import com.example.autopartsshop.utils.SharedPrefManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class EditProfileActivity extends AppCompatActivity {
    private static final String TAG = "EditProfileActivity";

    private EditText editTextName, editTextEmail, editTextPassword;
    private Button buttonCancel, buttonSave;
    private ImageView profileImage;
    private ImageButton buttonImageEditor;

    private Uri selectedImageUri;
    private Bitmap selectedBitmap;
    private byte[] selectedDrawableData;
    private UserDao userDao;
    private SharedPrefManager sharedPrefManager;
    private User currentUser;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Log.d(TAG, "onCreate started");

        userDao = new UserDao(this);
        sharedPrefManager = SharedPrefManager.getInstance(this);

        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonCancel = findViewById(R.id.buttonCancel);
        buttonSave = findViewById(R.id.buttonSave);
        profileImage = findViewById(R.id.editProfile);
        buttonImageEditor = findViewById(R.id.buttonImageEditor);

        currentUser = sharedPrefManager.getUser();

        if (currentUser != null) {
            editTextName.setText(currentUser.getUsername());
            editTextEmail.setText(currentUser.getEmail());
            
            // Display profile image if available
            byte[] profileImageData = currentUser.getProfileImage();
            if (profileImageData != null && profileImageData.length > 0) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(profileImageData, 0, profileImageData.length);
                selectedBitmap = bitmap; // Store the bitmap for editing
                profileImage.setImageBitmap(bitmap);
            }
        } else {
            Toast.makeText(this, "Error: User data not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set up click listeners
        buttonImageEditor.setOnClickListener(v -> openDrawableSelector());
        buttonCancel.setOnClickListener(v -> finish());
        buttonSave.setOnClickListener(v -> saveUserProfile());
    }

    // Keep this method for future reference (commented out)
    /*
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    */
    
    private void openDrawableSelector() {
        try {
            DrawableUtils.showDrawableSelector(this, (drawableResId, drawableData) -> {
                try {
                    // Store the drawable data for saving later
                    selectedDrawableData = drawableData;
                    selectedImageUri = null; // Clear any selected URI
                    
                    // Convert drawable to bitmap for editing
                    Bitmap drawableBitmap = BitmapFactory.decodeByteArray(drawableData, 0, drawableData.length);
                    selectedBitmap = drawableBitmap;

                    // Display the selected drawable in the image view
                    if (drawableData != null) {
                        profileImage.setImageResource(drawableResId);
                        Toast.makeText(this, "Car brand selected", Toast.LENGTH_SHORT).show();
                    } else {
                        // If we couldn't get the drawable data, use a default icon
                        profileImage.setImageResource(R.drawable.ic_default_brand);
                        Toast.makeText(this, "Using default image", Toast.LENGTH_SHORT).show();
                    }
                } catch (Resources.NotFoundException e) {
                    Log.e(TAG, "Resource not found: " + drawableResId, e);
                    // Use a default icon if the resource is not found
                    profileImage.setImageResource(R.drawable.ic_default_brand);
                    Toast.makeText(this, "Error loading image, using default", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e(TAG, "Error setting image: " + e.getMessage(), e);
                    profileImage.setImageResource(R.drawable.ic_default_brand);
                    Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error showing drawable selector: " + e.getMessage(), e);
            Toast.makeText(this, "Could not open car brand selector", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void openImageEditor() {
        // Check if we have an image to edit
        if (selectedBitmap == null && profileImage.getDrawable() == null) {
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Create a bitmap from the current image if selectedBitmap is null
        if (selectedBitmap == null) {
            profileImage.buildDrawingCache();
            selectedBitmap = profileImage.getDrawingCache();
        }
        
        // Create an alert dialog with editing options
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Image");
        
        // Add editing options
        String[] options = {"Rotate Right", "Rotate Left", "Flip Horizontal", "Flip Vertical"};
        builder.setItems(options, (dialog, which) -> {
            Matrix matrix = new Matrix();
            
            switch (which) {
                case 0: // Rotate Right
                    matrix.postRotate(90);
                    break;
                case 1: // Rotate Left
                    matrix.postRotate(-90);
                    break;
                case 2: // Flip Horizontal
                    matrix.preScale(-1, 1);
                    break;
                case 3: // Flip Vertical
                    matrix.preScale(1, -1);
                    break;
            }
            
            // Apply transformation to bitmap
            try {
                Bitmap editedBitmap = Bitmap.createBitmap(selectedBitmap, 0, 0, 
                        selectedBitmap.getWidth(), selectedBitmap.getHeight(), matrix, true);
                
                // Update the image view and store the edited bitmap
                profileImage.setImageBitmap(editedBitmap);
                selectedBitmap = editedBitmap;
                
                // Clear drawable data since we're using a bitmap now
                selectedDrawableData = null;
                selectedImageUri = null;
                
                Toast.makeText(this, "Image edited successfully", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "Error editing image: " + e.getMessage(), e);
                Toast.makeText(this, "Error editing image", Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            selectedDrawableData = null; // Clear any selected drawable data
            try {
                selectedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                profileImage.setImageBitmap(selectedBitmap);
            } catch (IOException e) {
                Log.e(TAG, "Error loading image: " + e.getMessage(), e);
            }
        }
    }

    private void saveUserProfile() {
        try {
            String name = editTextName.getText().toString().trim();
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }
            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Name and email are required", Toast.LENGTH_SHORT).show();
                return;
            }

            User existingUser = userDao.getUserByEmail(email);
            if (existingUser == null) {
                Log.e("UserDao", "User not found in database, update cannot proceed.");
                return;
            }

            // Prepare updated user object
            User updatedUser = new User();
            updatedUser.setId(existingUser.getId());
            updatedUser.setUsername(name);
            updatedUser.setEmail(email);
            updatedUser.setPhone(existingUser.getPhone());
            updatedUser.setAddress(existingUser.getAddress());
            updatedUser.setAdmin(existingUser.isAdmin());

            // Set password only if entered
            updatedUser.setPassword(password.isEmpty() ? existingUser.getPassword() : password);

            // Handle profile image update
            byte[] compressedImageBytes = existingUser.getProfileImage(); // Default: Keep old image
            
            if (selectedDrawableData != null) {
                // Use the selected drawable data directly
                compressedImageBytes = selectedDrawableData;
                Log.d(TAG, "Using drawable data for profile image");
            } else if (selectedBitmap != null) {
                // Resize and compress the selected image from gallery or edited bitmap
                Bitmap resizedBitmap = resizeBitmap(selectedBitmap, 300, 300);
                compressedImageBytes = compressBitmapToByteArray(resizedBitmap);
                Log.d(TAG, "Using bitmap for profile image");
            }
            
            updatedUser.setProfileImage(compressedImageBytes);

            // **Perform the database update**
            int rowsAffected = userDao.updateUser(updatedUser);
            Log.d("UserDao", "Update Result: Rows affected: " + rowsAffected);

            // **Handle SharedPreferences update**
            if (rowsAffected > 0) {
                sharedPrefManager.saveUser(updatedUser);
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e("UserDao", "Error in saveUserProfile: " + e.getMessage(), e);
            Toast.makeText(this, "Update error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private byte[] compressBitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream); // Compress to 80% quality
        return byteArrayOutputStream.toByteArray();
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
