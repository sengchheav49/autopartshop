package com.example.autopartsshop.activities;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.autopartsshop.R;
import com.example.autopartsshop.database.DatabaseHelper;
import com.example.autopartsshop.database.UserDao;
import com.example.autopartsshop.models.User;
import com.example.autopartsshop.utils.SharedPrefManager;
import com.google.android.material.textfield.TextInputLayout;

public class AdminLoginActivity extends AppCompatActivity {

    private TextInputLayout textInputLayoutEmail, textInputLayoutPassword;
    private EditText editTextEmail, editTextPassword;
    private Button buttonAdminLogin;

    private UserDao userDao;
    private SharedPrefManager sharedPrefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        // Initialize DAO and shared preferences
        userDao = new UserDao(this);
        sharedPrefManager = SharedPrefManager.getInstance(this);

        // Initialize views
        initViews();

        // Setup click listeners
        setUpListeners();
    }

    private boolean validateAdminLogin(String email, String password) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        boolean isValid = false;

        // First check in USERS table
        Cursor userCursor = db.query(
                DatabaseHelper.TABLE_USERS,
                null,
                DatabaseHelper.KEY_EMAIL + " = ? AND " + DatabaseHelper.KEY_IS_ADMIN + " = ?",
                new String[]{email, "1"},
                null, null, null
        );

        if (userCursor != null && userCursor.moveToFirst()) {
            int passwordColumnIndex = userCursor.getColumnIndex(DatabaseHelper.KEY_PASSWORD);
            if (passwordColumnIndex != -1) {
                String storedPassword = userCursor.getString(passwordColumnIndex);
                isValid = password.equals(storedPassword);
                Log.d("AdminLogin", "User table password: " + storedPassword + ", match: " + isValid);
            }
            userCursor.close();
        }

        // If not found in USERS, try ADMINS table
        if (!isValid) {
            try {
                Cursor adminCursor = db.query(
                        DatabaseHelper.TABLE_ADMINS,
                        null,
                        DatabaseHelper.KEY_EMAIL + " = ?",
                        new String[]{email},
                        null, null, null
                );

                if (adminCursor != null && adminCursor.moveToFirst()) {
                    int passwordColumnIndex = adminCursor.getColumnIndex(DatabaseHelper.KEY_PASSWORD);
                    if (passwordColumnIndex != -1) {
                        String storedPassword = adminCursor.getString(passwordColumnIndex);
                        isValid = password.equals(storedPassword);
                        Log.d("AdminLogin", "Admin table password: " + storedPassword + ", match: " + isValid);
                    }
                    adminCursor.close();
                }
            } catch (Exception e) {
                // Table might not exist, that's ok
                Log.e("AdminLogin", "Error checking admin table: " + e.getMessage());
            }
        }

        db.close();
        return isValid;
    }

    private void initViews() {
        textInputLayoutEmail = findViewById(R.id.textInputLayoutAdminEmail);
        textInputLayoutPassword = findViewById(R.id.textInputLayoutAdminPassword);
        editTextEmail = findViewById(R.id.editTextAdminEmail);
        editTextPassword = findViewById(R.id.editTextAdminPassword);
        buttonAdminLogin = findViewById(R.id.buttonAdminLogin);
    }

    private void setUpListeners() {
        // Login button click
        buttonAdminLogin.setOnClickListener(v -> loginAdmin());
    }

    private void loginAdmin() {
        // Get input values
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Validate inputs
        if (!validateInputs(email, password)) {
            return;
        }

        // Use the improved validation method to check both tables
        if (validateAdminLogin(email, password)) {
            // Get user from database
            User user = userDao.getUserByEmail(email);

            if (user != null) {
                // Save user session
                sharedPrefManager.setUserLogin(user);


                // Navigate to admin dashboard
                startActivity(new Intent(this, AdminDashboardActivity.class));
                finish();
            } else {
                // This shouldn't happen if validateAdminLogin returned true
                // But we'll handle it just in case
                Toast.makeText(this, "Error retrieving admin account", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Login failed
            Toast.makeText(this, "Invalid admin credentials", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInputs(String email, String password) {
        boolean isValid = true;

        // Validate email
        if (TextUtils.isEmpty(email)) {
            textInputLayoutEmail.setError("Email is required");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            textInputLayoutEmail.setError("Please enter a valid email");
            isValid = false;
        } else {
            textInputLayoutEmail.setError(null);
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            textInputLayoutPassword.setError("Password is required");
            isValid = false;
        } else {
            textInputLayoutPassword.setError(null);
        }

        return isValid;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Helper method for debugging purposes - you can call this from a button or menu option
    private void debugShowPassword(String email) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Check USERS table
        Cursor userCursor = db.query(
                DatabaseHelper.TABLE_USERS,
                new String[]{DatabaseHelper.KEY_PASSWORD},
                DatabaseHelper.KEY_EMAIL + " = ?",
                new String[]{email},
                null, null, null
        );

        String userPassword = "Not found";
        if (userCursor != null && userCursor.moveToFirst()) {
            userPassword = userCursor.getString(userCursor.getColumnIndexOrThrow(DatabaseHelper.KEY_PASSWORD));
            userCursor.close();
        }

        // Check ADMINS table
        String adminPassword = "Not found";
        try {
            Cursor adminCursor = db.query(
                    DatabaseHelper.TABLE_ADMINS,
                    new String[]{DatabaseHelper.KEY_PASSWORD},
                    DatabaseHelper.KEY_EMAIL + " = ?",
                    new String[]{email},
                    null, null, null
            );

            if (adminCursor != null && adminCursor.moveToFirst()) {
                adminPassword = adminCursor.getString(adminCursor.getColumnIndexOrThrow(DatabaseHelper.KEY_PASSWORD));
                adminCursor.close();
            }
        } catch (Exception e) {
            // ADMINS table might not exist
        }

        db.close();

        // Display the passwords (FOR DEBUGGING ONLY - remove in production!)
        Toast.makeText(this,
                "User password: " + userPassword + "\nAdmin password: " + adminPassword,
                Toast.LENGTH_LONG).show();
    }
}