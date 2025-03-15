package com.example.autopartsshop.utils;

import static com.example.autopartsshop.utils.ImageUtils.base64ToBitmap;
import static com.example.autopartsshop.utils.ImageUtils.bitmapToBase64;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.example.autopartsshop.models.User;

import java.io.ByteArrayOutputStream;

public class SharedPrefManager {
    private static final String TAG = "SharedPrefManager";
    private static final String SHARED_PREF_NAME = "autopartsshop_prefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_IS_ADMIN = "is_admin";
     static final String KEY_USER_IMAGE = "user_image";

    private static SharedPrefManager instance;
    private final SharedPreferences sharedPreferences;

    private SharedPrefManager(Context context) {
        this.sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SharedPrefManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefManager(context);
        }
        return instance;
    }

    public void saveUser(User user) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(KEY_USER_ID, user.getId());
        editor.putString(KEY_USERNAME, user.getUsername());
        editor.putString(KEY_EMAIL, user.getEmail());
        editor.putString(KEY_PASSWORD, user.getPassword());
        editor.putString(KEY_PHONE, user.getPhone());
        editor.putString(KEY_ADDRESS, user.getAddress());
        editor.putBoolean(KEY_IS_ADMIN, user.isAdmin());

        if (user.getProfileImage() != null) {
            String encodedImage = Base64.encodeToString(user.getProfileImage(), Base64.DEFAULT);
            editor.putString(KEY_USER_IMAGE, encodedImage);
        }

        editor.apply();
    }
    public User getUser() {

        int userId = sharedPreferences.getInt(KEY_USER_ID, -1);
        String username = sharedPreferences.getString(KEY_USERNAME, null);
        String email = sharedPreferences.getString(KEY_EMAIL, null);
        String password = sharedPreferences.getString(KEY_PASSWORD, null);
        String phone = sharedPreferences.getString(KEY_PHONE, null);
        String address = sharedPreferences.getString(KEY_ADDRESS, null);
        boolean isAdmin = sharedPreferences.getBoolean(KEY_IS_ADMIN, false);

        // Retrieve and decode the stored Base64 image string
        String encodedImage = sharedPreferences.getString(KEY_USER_IMAGE, null);
        byte[] profileImage = null;

        if (encodedImage != null) {
            profileImage = Base64.decode(encodedImage, Base64.DEFAULT);
        }

        if (userId == -1 || username == null || email == null) {
            return null;
        }

        return new User(username, email, password,phone,address, profileImage);
    }
    public void updateUser(User user) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(KEY_USER_ID, user.getId());
        editor.putString(KEY_USERNAME, user.getUsername());
        editor.putString(KEY_EMAIL, user.getEmail());
        editor.putString(KEY_PASSWORD, user.getPassword());
        editor.putString(KEY_PHONE, user.getPhone());
        editor.putString(KEY_ADDRESS, user.getAddress());
        editor.putBoolean(KEY_IS_ADMIN, user.isAdmin());

        if (user.getProfileImage() != null) {
            String encodedImage = Base64.encodeToString(user.getProfileImage(), Base64.DEFAULT);
            editor.putString(KEY_USER_IMAGE, encodedImage);
        } else {
            editor.remove(KEY_USER_IMAGE); // Ensure old images are removed if there's none
        }

        editor.apply();
    }

    public String getUserImage() {
        return sharedPreferences.getString(KEY_USER_IMAGE, null);
    }

    private Bitmap byteArrayToBitmap(byte[] byteArray) {
        if (byteArray == null || byteArray.length == 0) return null;
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }


    private byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getInt(KEY_USER_ID, -1) != -1;
    }

    public boolean isAdmin() {
        return sharedPreferences.getBoolean(KEY_IS_ADMIN, false);
    }

    public void setUserLogin(User user) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(KEY_USER_ID, user.getId());
        editor.putString(KEY_USERNAME, user.getUsername());
        editor.putString(KEY_EMAIL, user.getEmail());
        editor.putString(KEY_PASSWORD, user.getPassword());
        editor.putString(KEY_PHONE, user.getPhone());
        editor.putString(KEY_ADDRESS, user.getAddress());
        editor.putBoolean(KEY_IS_ADMIN, user.isAdmin());

        editor.apply();

        Log.d(TAG, "User saved to SharedPreferences: " + user.toString());
    }

    public void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
