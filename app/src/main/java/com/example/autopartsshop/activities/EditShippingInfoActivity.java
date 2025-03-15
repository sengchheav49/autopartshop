package com.example.autopartsshop.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.autopartsshop.R;
import com.example.autopartsshop.database.UserDao;
import com.example.autopartsshop.models.User;
import com.example.autopartsshop.utils.SharedPrefManager;

public class EditShippingInfoActivity extends AppCompatActivity {
    private static final String TAG = "EditShippingInfo";

    // Views
    private EditText editTextPhone;
    private EditText editTextAddress;
    private Button buttonSave;
    private Button buttonCancel;

    // Data
    private SharedPrefManager sharedPrefManager;
    private UserDao userDao;
    private User currentUser;
    private String textEmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_edit_shipping_info);

            // Set up action bar
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Edit Shipping Information");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            sharedPrefManager = SharedPrefManager.getInstance(this);
            userDao = new UserDao(this);
            textEmail = sharedPrefManager.getUser().getEmail();
            currentUser = userDao.getUserByEmail(textEmail);

            // Find views
            editTextPhone = findViewById(R.id.editTextPhoneinfo);
            editTextAddress = findViewById(R.id.editTextAddresss);
            buttonSave = findViewById(R.id.buttonSave);
            buttonCancel = findViewById(R.id.buttonCancel);

            if (currentUser == null) {
                Toast.makeText(this, "Error: User not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Set the current phone and address from SQLite
            editTextPhone.setText(currentUser.getPhone() != null ? currentUser.getPhone() : "");
            editTextAddress.setText(currentUser.getAddress() != null ? currentUser.getAddress() : "");


            // Set up listeners
            setupListeners();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            finish();
        }


    }


    private void setupListeners() {
        // Save button
        buttonSave.setOnClickListener(v -> {
            try {
                saveShippingInfo(textEmail);
//

            } catch (Exception e) {
                Log.e(TAG, "Error saving: " + e.getMessage(), e);
                Toast.makeText(this, "Error saving shipping information", Toast.LENGTH_SHORT).show();
            }
        });

        // Cancel button
        if (buttonCancel != null) {
            buttonCancel.setOnClickListener(v -> finish());
        }
    }

    private void saveShippingInfo(String userEmail) {
        try {
            // Get input values
            String phone = editTextPhone.getText().toString().trim();
            String address = editTextAddress.getText().toString().trim();

            // Validate inputs
            if (phone.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "Phone number and shipping address are required", Toast.LENGTH_SHORT).show();
                if (phone.isEmpty()) {
                    editTextPhone.setError("Phone number is required");
                }
                if (address.isEmpty()) {
                    editTextAddress.setError("Shipping address is required");
                }
                return;
            }

            // Fetch the existing user from the database using the provided email
            User existingUser = userDao.getUserByEmail(userEmail);
            if (existingUser == null) {
                return;
            }

            // Create an updated user object
            User updatedUser = new User();
            updatedUser.setId(existingUser.getId());  // Ensure correct ID
            updatedUser.setUsername(existingUser.getUsername());
            updatedUser.setEmail(existingUser.getEmail());
            updatedUser.setPassword(existingUser.getPassword());
            updatedUser.setPhone(phone);
            updatedUser.setAddress(address);
            updatedUser.setAdmin(existingUser.isAdmin());
            updatedUser.setProfileImage(existingUser.getProfileImage()); // Preserve profile image

            // Update the user in the database
            int rowsAffected = userDao.updateUser(updatedUser);

            if (rowsAffected > 0) {
                // Update SharedPreferences
                sharedPrefManager.saveUser(updatedUser);
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Failed to update shipping information", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Update error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}