package com.example.autopartsshop.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.autopartsshop.R;
import com.example.autopartsshop.database.UserDao;
import com.example.autopartsshop.models.User;
import com.example.autopartsshop.utils.SharedPrefManager;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout textInputLayoutEmail, textInputLayoutPassword;
    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin, buttonRegister;
    private TextView textViewLoginAsAdmin;

    private UserDao userDao;
    private SharedPrefManager sharedPrefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize DAO and shared preferences
        userDao = new UserDao(this);
        sharedPrefManager = SharedPrefManager.getInstance(this);

        // Check if already logged in
        if (sharedPrefManager.isLoggedIn()) {
            if (sharedPrefManager.isAdmin()) {
                startActivity(new Intent(this, AdminDashboardActivity.class));
            } else {
                startActivity(new Intent(this, MainActivity.class));
            }
            finish();
            return;
        }

        // Initialize views
        initViews();

        // Setup click listeners
        setUpListeners();

        // Print all users for debugging (optional)
        userDao.printAllUsers();
    }

    private void initViews() {
        textInputLayoutEmail = findViewById(R.id.textInputLayoutEmail);
        textInputLayoutPassword = findViewById(R.id.textInputLayoutPassword);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonRegister = findViewById(R.id.buttonRegister);
        textViewLoginAsAdmin = findViewById(R.id.textViewLoginAsAdmin);
    }

    private void setUpListeners() {
        // Login button click
        buttonLogin.setOnClickListener(v -> loginUser());

        // Register button click
        buttonRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        // Admin login click
        textViewLoginAsAdmin.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, AdminLoginActivity.class));
        });
    }

    private void loginUser() {
        // Get input values
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Validate inputs
        if (!validateInputs(email, password)) {
            return;
        }

        // Check login credentials
        User user = userDao.checkLogin(email, password);

        if (user != null) {
            Log.d("LOGIN_DEBUG", "User found - Username: " + user.getUsername() + ", Is Admin: " + user.isAdmin());

            // Save user session
            sharedPrefManager.setUserLogin(user);

            // Navigate based on user type
            if (user.isAdmin()) {
                startActivity(new Intent(this, AdminDashboardActivity.class));
            } else {
                startActivity(new Intent(this, MainActivity.class));
            }

            finish();
        } else {
            Log.d("LOGIN_DEBUG", "Login failed for email: " + email);
            // Login failed
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
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
        } else if (password.length() < 6) {
            textInputLayoutPassword.setError("Password must be at least 6 characters");
            isValid = false;
        } else {
            textInputLayoutPassword.setError(null);
        }

        return isValid;
    }
}