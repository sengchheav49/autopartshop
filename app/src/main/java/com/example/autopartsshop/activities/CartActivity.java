package com.example.autopartsshop.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.autopartsshop.R;
import com.example.autopartsshop.adapters.CartAdapter;
import com.example.autopartsshop.database.CartDao;
import com.example.autopartsshop.models.CartItem;
import com.example.autopartsshop.utils.SharedPrefManager;

import java.util.List;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerViewCartItems;
    private LinearLayout layoutEmptyCart;
    private TextView textViewSubtotalValue;
    private TextView textViewTaxValue;
    private TextView textViewTotalValue;
    private Button buttonCheckout;
    private Button buttonStartShopping;

    private CartDao cartDao;
    private SharedPrefManager sharedPrefManager;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItems;

    // Tax rate as a decimal (8%)
    private static final double TAX_RATE = 0.08;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        cartDao = new CartDao(this);
        sharedPrefManager = SharedPrefManager.getInstance(this);

        initViews();

        if (!sharedPrefManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        loadCartItems();
        setupListeners();
    }


    private void initViews() {
        recyclerViewCartItems = findViewById(R.id.recyclerViewCartItems);
        layoutEmptyCart = findViewById(R.id.layoutEmptyCart);
        textViewSubtotalValue = findViewById(R.id.textViewSubtotalValue);
        textViewTaxValue = findViewById(R.id.textViewTaxValue);
        textViewTotalValue = findViewById(R.id.textViewTotalValue);
        buttonCheckout = findViewById(R.id.buttonCheckout);
        buttonStartShopping = findViewById(R.id.buttonStartShopping);
    }

    private void loadCartItems() {
        int userId = sharedPrefManager.getUser().getId();
        cartItems = cartDao.getCartItems(userId);

        if (cartItems.isEmpty()) {
            // Show empty cart view
            showEmptyCart();
            return;
        }

        // Show cart items
        showCartItems();

        // Set up adapter
        cartAdapter = new CartAdapter(cartItems);
        recyclerViewCartItems.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCartItems.setAdapter(cartAdapter);

        // Calculate totals
        calculateTotals();

        // Set up adapter callbacks
        setupAdapterCallbacks();
    }

    private void setupAdapterCallbacks() {
        // Update item quantity
        cartAdapter.setOnQuantityChangeListener((cartItem, newQuantity) -> {
            cartDao.updateCartItemQuantity(cartItem.getId(), newQuantity);
            calculateTotals();
        });

        // Delete item
        cartAdapter.setOnDeleteClickListener(cartItem -> {
            cartDao.removeFromCart(cartItem.getId());
            cartItems.remove(cartItem);
            cartAdapter.notifyDataSetChanged();

            if (cartItems.isEmpty()) {
                showEmptyCart();
            } else {
                calculateTotals();
            }
        });
    }

    private void calculateTotals() {
        double subtotal = 0;

        for (CartItem item : cartItems) {
            subtotal += item.getSubtotal();
        }

        double tax = subtotal * TAX_RATE;
        double total = subtotal + tax;

        // Format and display values
        textViewSubtotalValue.setText(String.format("$%.2f", subtotal));
        textViewTaxValue.setText(String.format("$%.2f", tax));
        textViewTotalValue.setText(String.format("$%.2f", total));
    }

    private void showCartItems() {
        recyclerViewCartItems.setVisibility(View.VISIBLE);
        layoutEmptyCart.setVisibility(View.GONE);
    }

    private void showEmptyCart() {
        recyclerViewCartItems.setVisibility(View.GONE);
        layoutEmptyCart.setVisibility(View.VISIBLE);
    }

    private void setupListeners() {
        // Checkout button
        buttonCheckout.setOnClickListener(v -> {
            if (cartItems != null && !cartItems.isEmpty()) {
                Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
                startActivity(intent);
            }
        });

        // Start shopping button
        buttonStartShopping.setOnClickListener(v -> {
            finish(); // Go back to the previous screen
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload cart items to reflect any changes
        if (sharedPrefManager.isLoggedIn()) {
            loadCartItems();
        }
    }
}