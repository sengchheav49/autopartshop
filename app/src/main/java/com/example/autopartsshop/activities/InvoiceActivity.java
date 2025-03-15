package com.example.autopartsshop.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.autopartsshop.R;
import com.example.autopartsshop.adapters.InvoiceItemAdapter;
import com.example.autopartsshop.database.OrderDao;
import com.example.autopartsshop.database.UserDao;
import com.example.autopartsshop.models.Order;
import com.example.autopartsshop.models.User;
import com.example.autopartsshop.utils.SharedPrefManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class InvoiceActivity extends AppCompatActivity {

    private TextView textViewOrderNumber;
    private TextView textViewOrderDate;
    private TextView textViewPaymentMethod;
    private TextView textViewCustomerName;
    private TextView textViewCustomerAddress;
    private TextView textViewCustomerPhone;
    private TextView textViewCustomerEmail;
    private RecyclerView recyclerViewInvoiceItems;
    private TextView textViewInvoiceSubtotal;
    private TextView textViewInvoiceTax;
    private TextView textViewInvoiceTotal;
    private Button buttonDownloadInvoice;
    private Button buttonContinueShopping;

    private OrderDao orderDao;
    private UserDao userDao;
    private SharedPrefManager sharedPrefManager;

    private Order order;
    private User user;
    String textEmail;


    // Tax rate as a decimal (8%)
    private static final double TAX_RATE = 0.08;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice);
        sharedPrefManager = SharedPrefManager.getInstance(this);
        userDao = new UserDao(this);

        // Get user email from SharedPreferences
        textEmail = sharedPrefManager.getUser().getEmail();
        // Initialize DAOs and SharedPrefManager
        orderDao = new OrderDao(this);
        userDao = new UserDao(this);
        sharedPrefManager = SharedPrefManager.getInstance(this);

        // Get order ID from intent
        int orderId = getIntent().getIntExtra("order_id", -1);
        String paymentMethod = getIntent().getStringExtra("payment_method");

        if (orderId == -1) {
            finish();
            return;
        }

        // Get order from database
        order = orderDao.getOrderById(orderId);
        Log.d("OrderId", "OrderId is " + order.getId());

        user = userDao.getUserByEmail(textEmail);

        if (user == null) {
            Log.e("OrderId", "User is null for order ID: " + order.getId());
        } else {
            Log.d("OrderId", "User ID is " + user.getId());
        }
        initViews();

        // Set up toolbar


        // Display invoice information
        displayInvoiceInfo(paymentMethod);

        // Set up listeners
        setupListeners();
    }

    private void initViews() {
        textViewOrderNumber = findViewById(R.id.textViewOrderNumber);
        textViewOrderDate = findViewById(R.id.textViewOrderDate);
        textViewPaymentMethod = findViewById(R.id.textViewPaymentMethod);
        textViewCustomerName = findViewById(R.id.textViewCustomerName);
        textViewCustomerAddress = findViewById(R.id.textViewCustomerAddress);
        textViewCustomerPhone = findViewById(R.id.textViewCustomerPhone);
        textViewCustomerEmail = findViewById(R.id.textViewCustomerEmail);
        recyclerViewInvoiceItems = findViewById(R.id.recyclerViewInvoiceItems);
        textViewInvoiceSubtotal = findViewById(R.id.textViewInvoiceSubtotal);
        textViewInvoiceTax = findViewById(R.id.textViewInvoiceTax);
        textViewInvoiceTotal = findViewById(R.id.textViewInvoiceTotal);
        buttonDownloadInvoice = findViewById(R.id.buttonDownloadInvoice);
        buttonContinueShopping = findViewById(R.id.buttonContinueShopping);
    }

    private void displayInvoiceInfo(String paymentMethod) {
        // Order info
        textViewOrderNumber.setText(order.getOrderNumber());

        // Format date
        String formattedDate = formatDate(order.getCreatedAt());
        textViewOrderDate.setText(formattedDate);

        // Payment method
        textViewPaymentMethod.setText(paymentMethod);

        // Customer info
        textViewCustomerName.setText(user.getUsername());
        textViewCustomerAddress.setText(user.getAddress());
        textViewCustomerPhone.setText(user.getPhone());
        textViewCustomerEmail.setText(user.getEmail());

        // Set up order items adapter
        InvoiceItemAdapter adapter = new InvoiceItemAdapter(order.getOrderItems());
        recyclerViewInvoiceItems.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewInvoiceItems.setAdapter(adapter);

        // Calculate subtotal, tax, and total
        double subtotal = calculateSubtotal();
        double tax = subtotal * TAX_RATE;
        double total = subtotal + tax;

        // Display totals
        textViewInvoiceSubtotal.setText(String.format("$%.2f", subtotal));
        textViewInvoiceTax.setText(String.format("$%.2f", tax));
        textViewInvoiceTotal.setText(String.format("$%.2f", total));
    }

    private double calculateSubtotal() {
        double subtotal = 0;

        for (Order.OrderItem item : order.getOrderItems()) {
            subtotal += item.getSubtotal();
        }

        return subtotal;
    }

    public static String formatDate(Date date) {
        try {
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return outputFormat.format(date);
        } catch (Exception e) {
            Log.e("DateFormat", "Error formatting date: " + date, e);
            return "Invalid Date";
        }
    }



    private void setupListeners() {
        // Download invoice
        buttonDownloadInvoice.setOnClickListener(v -> {
            // Simulate download delay
            buttonDownloadInvoice.setEnabled(false);
            buttonDownloadInvoice.setText(R.string.loading);

            new android.os.Handler().postDelayed(() -> {
                buttonDownloadInvoice.setEnabled(true);
                buttonDownloadInvoice.setText(R.string.download);
                android.widget.Toast.makeText(this, "Invoice downloaded", android.widget.Toast.LENGTH_SHORT).show();
            }, 2000);
        });

        // Continue shopping
        buttonContinueShopping.setOnClickListener(v -> {
            // Go to main activity
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Go to main activity instead of back navigation
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}