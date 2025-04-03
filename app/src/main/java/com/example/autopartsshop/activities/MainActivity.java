package com.example.autopartsshop.activities;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.autopartsshop.R;
import com.example.autopartsshop.database.UserDao;
import com.example.autopartsshop.fragments.CategoriesFragment;
import com.example.autopartsshop.fragments.HomeFragment;
import com.example.autopartsshop.fragments.ProfileFragment;
import com.example.autopartsshop.models.User;
import com.example.autopartsshop.utils.SharedPrefManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        
        // Check cart items after database sync
        checkCartItemsAfterSync();
    }

    private void initViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            
            if (itemId == R.id.navigation_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.navigation_categories) {
                selectedFragment = new CategoriesFragment();
            } else if (itemId == R.id.navigation_profile) {
                selectedFragment = new ProfileFragment();
            } else if (itemId == R.id.navigation_cart) {
                // Open cart activity when cart icon is clicked
                startActivity(new Intent(this, CartActivity.class));
                return true;
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });

        // Load home fragment by default
        loadFragment(new HomeFragment());
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        // Add debug option to check cart data
        if (menu.findItem(R.id.action_check_cart) == null) {
            menu.add(Menu.NONE, R.id.action_check_cart, Menu.NONE, "Debug: Check Cart");
        }
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_cart) {
            startActivity(new Intent(this, CartActivity.class));
            return true;
        } else if (itemId == R.id.action_search) {
            // Handle search action (we could implement a search activity)
            return true;
        } else if (itemId == R.id.action_check_cart) {
            // Check cart data
            checkCartData();
            return true;
        } else if (itemId == R.id.action_logout) {
            // Logout the user
            SharedPrefManager.getInstance(this).logout();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Debug method to check cart data in database
     */
    private void checkCartData() {
        try {
            // Get database path
            File dbFile = getDatabasePath("autoparts_db.db");
            
            // Open the database
            SQLiteDatabase db = SQLiteDatabase.openDatabase(dbFile.getPath(), null, SQLiteDatabase.OPEN_READONLY);
            
            // Get all table names
            Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
            StringBuilder tables = new StringBuilder("Database tables: ");
            while (c.moveToNext()) {
                tables.append(c.getString(0)).append(", ");
            }
            c.close();
            
            // Look for cart table and cart data
            Cursor cartCursor = null;
            String cartInfo = "";
            try {
                cartCursor = db.rawQuery("SELECT COUNT(*) FROM cart", null);
                if (cartCursor.moveToFirst()) {
                    int count = cartCursor.getInt(0);
                    cartInfo = "Found " + count + " items in cart table";
                }
            } catch (Exception e) {
                cartInfo = "Error reading cart table: " + e.getMessage();
            } finally {
                if (cartCursor != null) cartCursor.close();
            }
            
            // Close database
            db.close();
            
            // Show info
            Toast.makeText(this, tables.toString() + "\n" + cartInfo, Toast.LENGTH_LONG).show();
            
        } catch (Exception e) {
            Toast.makeText(this, "Database check error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Shows cart item count after database sync
     */
    private void checkCartItemsAfterSync() {
        try {
            SharedPrefManager prefManager = SharedPrefManager.getInstance(this);
            if (prefManager.isLoggedIn()) {
                User user = prefManager.getUser();
                if (user != null) {
                    // Import CartDao to check cart items
                    com.example.autopartsshop.database.CartDao cartDao = 
                        new com.example.autopartsshop.database.CartDao(this);
                    int cartCount = cartDao.getCartItemCount(user.getId());
                    
                    if (cartCount > 0) {
                        Toast.makeText(this, 
                            "You have " + cartCount + " items in your cart", 
                            Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error checking cart: " + e.getMessage());
        }
    }
}