package com.example.autopartsshop.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.autopartsshop.R;
import com.example.autopartsshop.utils.DatabaseManager;
import com.example.autopartsshop.utils.DatabaseSyncService;
import com.example.autopartsshop.utils.DatabaseSyncUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseSyncActivity extends AppCompatActivity {
    private static final String TAG = "DatabaseSyncActivity";
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int FILE_SELECT_CODE = 101;
    
    private ListView deviceListView;
    private SimpleAdapter deviceAdapter;
    private List<Map<String, String>> deviceList;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_sync);
        
        // Initialize the DatabaseSyncUtils
        DatabaseSyncUtils.init(getApplicationContext());
        
        // Start the sync service
        startService(new Intent(this, DatabaseSyncService.class));
        
        // Initialize UI
        setupUI();
        
        // Request permissions
        requestPermissions();
        
        // Refresh device list
        refreshDeviceList();
    }
    
    private void setupUI() {
        // Setup device list view
        deviceListView = findViewById(R.id.deviceListView);
        deviceList = new ArrayList<>();
        deviceAdapter = new SimpleAdapter(
                this,
                deviceList,
                android.R.layout.simple_list_item_2,
                new String[]{"name", "ip"},
                new int[]{android.R.id.text1, android.R.id.text2}
        );
        deviceListView.setAdapter(deviceAdapter);
        
        // Setup swipe refresh
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(this::refreshDeviceList);

        // Setup export button
        Button exportButton = findViewById(R.id.exportButton);
        exportButton.setOnClickListener(v -> exportDatabase());
        
        // Setup import button
        Button importButton = findViewById(R.id.importButton);
        importButton.setOnClickListener(v -> importDatabase());

        // Setup sync button
        Button syncButton = findViewById(R.id.syncButton);
        syncButton.setOnClickListener(v -> syncWithSelectedDevice());
        
        // Setup device list click listener
        deviceListView.setOnItemClickListener((parent, view, position, id) -> {
            // Mark the selected device
            for (Map<String, String> device : deviceList) {
                device.put("selected", "false");
            }
            deviceList.get(position).put("selected", "true");
            deviceAdapter.notifyDataSetChanged();
        });
    }
    
    private void refreshDeviceList() {
        // Clear existing devices
        deviceList.clear();
        
        // Add discovered devices
        for (DatabaseSyncUtils.DeviceInfo device : DatabaseSyncUtils.getDiscoveredDevices()) {
            Map<String, String> deviceMap = new HashMap<>();
            deviceMap.put("name", device.deviceName);
            deviceMap.put("ip", device.ipAddress + ":" + device.port);
            deviceMap.put("selected", "false");
            deviceList.add(deviceMap);
        }

        // Notify adapter and stop refresh animation
        deviceAdapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
        
        // Show message if no devices found
        if (deviceList.isEmpty()) {
            Toast.makeText(this, "No devices found on network", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void exportDatabase() {
        // Use the DatabaseManager to export the database
        DatabaseManager dbManager = new DatabaseManager(this);
        boolean success = dbManager.exportDatabase();
        
        if (success) {
            Toast.makeText(this, "Database exported successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to export database", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void importDatabase() {
        // Open file picker
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, FILE_SELECT_CODE);
    }
    
    private void syncWithSelectedDevice() {
        // Find selected device
        String selectedIp = null;
        int selectedPort = 0;
        
        for (Map<String, String> device : deviceList) {
            if ("true".equals(device.get("selected"))) {
                String[] parts = device.get("ip").split(":");
                selectedIp = parts[0];
                selectedPort = Integer.parseInt(parts[1]);
                break;
            }
        }
        
        if (selectedIp == null) {
            Toast.makeText(this, "Please select a device first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Schedule sync with WorkManager
        DatabaseSyncUtils.scheduleDatabaseSync(this);
        Toast.makeText(this, "Synchronization started", Toast.LENGTH_SHORT).show();
    }
    
    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11 and above, request MANAGE_EXTERNAL_STORAGE permission
            if (!Environment.isExternalStorageManager()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Permission Required");
                builder.setMessage("This app needs storage permission to export/import database files.");
                builder.setPositiveButton("Grant", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                });
                builder.setNegativeButton("Cancel", null);
                builder.show();
            }
        } else {
            // For older versions, request regular storage permissions
            String[] permissions = {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.ACCESS_WIFI_STATE
            };
            
            List<String> permissionsToRequest = new ArrayList<>();
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(permission);
                }
            }
            
            if (!permissionsToRequest.isEmpty()) {
                ActivityCompat.requestPermissions(this, 
                        permissionsToRequest.toArray(new String[0]), 
                        PERMISSION_REQUEST_CODE);
            }
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            
            if (!allGranted) {
                Toast.makeText(this, "Permissions are required for database sync", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_SELECT_CODE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                boolean success = DatabaseSyncUtils.importDatabase(this, uri);
                if (success) {
                    Toast.makeText(this, "Database imported successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to import database", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        // Don't stop the service here to keep it running in background
        super.onDestroy();
    }
} 