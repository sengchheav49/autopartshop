package com.example.autopartsshop.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.autopartsshop.R;
import com.example.autopartsshop.database.CartDao;
import com.example.autopartsshop.database.OrderDao;
import com.example.autopartsshop.database.UserDao;
import com.example.autopartsshop.models.CartItem;
import com.example.autopartsshop.models.User;
import com.example.autopartsshop.utils.Constants;
import com.example.autopartsshop.utils.SharedPrefManager;

import java.util.List;

public class CheckoutActivity extends AppCompatActivity {

    private TextView textViewShippingName;
    private TextView textViewShippingAddress;
    private TextView textViewShippingPhone;
    private RadioGroup radioGroupPaymentMethod;
    private TextView textViewOrderItemsCount;
    private TextView textViewCheckoutTotalValue;
    private Button buttonViewOrderItems;
    private Button buttonEditShipping;
    private Button buttonPlaceOrder;

    private CartDao cartDao;
    private OrderDao orderDao;
    private SharedPrefManager sharedPrefManager;

    private List<CartItem> cartItems;
    private User user;
    private String textEmail;

    // Tax rate as a decimal (8%)
    private static final double TAX_RATE = 0.08;
    private static final int REQUEST_CODE_EDIT_SHIPPING = 1002;
    private UserDao userDao;
    User currentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        userDao = new UserDao(this);
        sharedPrefManager = SharedPrefManager.getInstance(this);
        userDao = new UserDao(this);
        cartDao = new CartDao(this);
        orderDao = new OrderDao(this);
        sharedPrefManager = SharedPrefManager.getInstance(this);
        user = sharedPrefManager.getUser();

        if (!sharedPrefManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        initViews();
        loadCartItems();
        displayShippingInfo();
        setupListeners();
        backButton();
    }


    private void backButton() {
        ImageButton productBackIcon = findViewById(R.id.checkoutBackIcon);
        productBackIcon.setOnClickListener(v -> finish()); // Closes the current activity
    }

    private void initViews() {
        textViewShippingName = findViewById(R.id.textViewShippingName);
        textViewShippingAddress = findViewById(R.id.textViewShippingAddress);
        textViewShippingPhone = findViewById(R.id.textViewShippingPhone);
        radioGroupPaymentMethod = findViewById(R.id.radioGroupPaymentMethod);
        textViewOrderItemsCount = findViewById(R.id.textViewOrderItemsCount);
        textViewCheckoutTotalValue = findViewById(R.id.textViewCheckoutTotalValue);
        buttonViewOrderItems = findViewById(R.id.buttonViewOrderItems);
        buttonEditShipping = findViewById(R.id.buttonEditShipping);
        buttonPlaceOrder = findViewById(R.id.buttonPlaceOrder);

        // Default payment method selection
        RadioButton radioButtonCashOnDelivery = findViewById(R.id.radioButtonCashOnDelivery);
        radioButtonCashOnDelivery.setChecked(true);
    }

    private void loadCartItems() {
        cartItems = cartDao.getCartItems(user.getId());

        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Display cart summary
        textViewOrderItemsCount.setText(String.format("%d items", cartItems.size()));

        // Calculate total
        double total = calculateTotal();
        textViewCheckoutTotalValue.setText(String.format("$%.2f", total));
    }

    private double calculateTotal() {
        double subtotal = 0;

        for (CartItem item : cartItems) {
            subtotal += item.getSubtotal();
        }

        double tax = subtotal * TAX_RATE;
        return subtotal + tax;
    }

    private void displayShippingInfo() {
        User loggedInUser = sharedPrefManager.getUser();
        String email = loggedInUser.getEmail();
        UserDao userDao = new UserDao(this);
        User sqlUser = userDao.getUserByEmail(email);

        if (sqlUser != null ) {
            textViewShippingName.setText(sqlUser.getUsername());
            textViewShippingAddress.setText(sqlUser.getAddress());
            textViewShippingPhone.setText(sqlUser.getPhone());
        }
    }

    private void setupListeners() {
        // View order items
        buttonViewOrderItems.setOnClickListener(v -> {
            onBackPressed(); // Go back to cart
        });

        // Edit shipping info
        buttonEditShipping.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditShippingInfoActivity.class);
            startActivityForResult(intent, REQUEST_CODE_EDIT_SHIPPING);
        });

        // Place order
        buttonPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_EDIT_SHIPPING && resultCode == Activity.RESULT_OK) {
            updateShippingInfoDisplay();
        }
    }

    private void updateShippingInfoDisplay() {
        User user = SharedPrefManager.getInstance(this).getUser();
        textViewShippingName.setText(user.getUsername());
        textViewShippingAddress.setText(user.getAddress());
        textViewShippingPhone.setText(user.getPhone());
    }

    private void placeOrder() {
        // Get payment method
        String paymentMethod = getSelectedPaymentMethod();

        // Create order
        long orderId = orderDao.createOrder(user.getId(), cartItems);

        if (orderId > 0) {
            // Order created successfully
            Intent intent = new Intent(this, InvoiceActivity.class);
            intent.putExtra("order_id", (int) orderId);
            intent.putExtra("payment_method", paymentMethod);
            startActivity(intent);
            finish();
        } else {
            // Order creation failed
            Toast.makeText(this, "Failed to place order", Toast.LENGTH_SHORT).show();
        }
    }

    private String getSelectedPaymentMethod() {
        int selectedId = radioGroupPaymentMethod.getCheckedRadioButtonId();

        if (selectedId == R.id.radioButtonCashOnDelivery) {
            return Constants.PAYMENT_METHOD_CASH;
        } else if (selectedId == R.id.radioButtonCreditCard) {
            return Constants.PAYMENT_METHOD_CREDIT_CARD;
        } else if (selectedId == R.id.radioButtonPaypal) {
            return Constants.PAYMENT_METHOD_PAYPAL;
        }

        return Constants.PAYMENT_METHOD_CASH; // Default
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}