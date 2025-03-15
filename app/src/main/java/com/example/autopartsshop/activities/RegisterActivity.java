package com.example.autopartsshop.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.autopartsshop.R;
import com.example.autopartsshop.database.UserDao;
import com.example.autopartsshop.models.User;
import com.example.autopartsshop.utils.SharedPrefManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextEmail, editTextPassword, editTextPhone, editTextAddress;
    private Button buttonRegister, buttonGoToLogin;
    private ImageButton buttonAddProfile;
    private ImageView profileImage;
    private Uri selectedImageUri;
    private UserDao userDao;
    private SharedPrefManager sharedPrefManager;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Bitmap selectedBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userDao = new UserDao(this);
        sharedPrefManager = SharedPrefManager.getInstance(this);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextAddress = findViewById(R.id.editTextAddress);
        buttonRegister = findViewById(R.id.buttonRegister);
        buttonGoToLogin = findViewById(R.id.buttonGoToLogin);
        profileImage = findViewById(R.id.addProfile);

        buttonRegister.setOnClickListener(v -> registerUser());
        buttonGoToLogin.setOnClickListener(v -> finish()); // Go back to login screen


    }

    private void registerUser() {
        buttonRegister.setEnabled(false);

        String username = editTextUsername.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)
                || TextUtils.isEmpty(phone) || TextUtils.isEmpty(address)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            buttonRegister.setEnabled(true);
            return;
        }
        if (password.length() < 6) {
            editTextPassword.setError("Password must be at least 6 characters");
            buttonRegister.setEnabled(true);
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Enter a valid email address", Toast.LENGTH_SHORT).show();
            buttonRegister.setEnabled(true);
            return;
        }

        if (userDao.isEmailExists(email)) {
            Toast.makeText(this, "Email is already registered", Toast.LENGTH_SHORT).show();
            buttonRegister.setEnabled(true);
            return;
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPassword(password);
        newUser.setPhone(phone);
        newUser.setAddress(address);
        newUser.setAdmin(false);

        // Insert user into the database
        long userId = userDao.createUser(newUser);
        if (userId > 0) {
            newUser.setId((int) userId);

            // Save user data in SharedPreferences
            sharedPrefManager.saveUser(newUser);

            Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show();
        }

        buttonRegister.setEnabled(true);
    }



}
